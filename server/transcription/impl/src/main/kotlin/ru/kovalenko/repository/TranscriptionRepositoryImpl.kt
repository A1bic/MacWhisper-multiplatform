package ru.kovalenko.repository

import org.jooq.DSLContext
import org.jooq.impl.DSL
import ru.kovalenko.TranscriptionId
import ru.kovalenko.repository.entity.NewSegment
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.repository.entity.TranscriptionEntity
import ru.kovalenko.repository.mappers.toEntity
import ru.kovalenko.transcription.jooq.enums.JobStatus
import ru.kovalenko.transcription.jooq.tables.references.TRANSCRIPTIONS
import ru.kovalenko.transcription.jooq.tables.references.TRANSCRIPT_SEGMENTS
import java.time.OffsetDateTime
import kotlin.uuid.toJavaUuid

class TranscriptionRepositoryImpl(private val dsl: DSLContext): TranscriptionRepository {
    override fun save(t: NewTranscription): TranscriptionEntity =
        dsl.insertInto(TRANSCRIPTIONS)
            .set(TRANSCRIPTIONS.RECORDING_ID, t.recordingId.value.toJavaUuid())
            .set(TRANSCRIPTIONS.MODEL, t.model)
            .set(TRANSCRIPTIONS.LANGUAGE, t.language)
            .returning()
            .fetchSingle()
            .toEntity()


    override fun findById(id: TranscriptionId): TranscriptionEntity? =
        dsl.selectFrom(TRANSCRIPTIONS)
            .where(TRANSCRIPTIONS.ID.eq(id.value.toJavaUuid()))
            .fetchOne()
            ?.toEntity()

    override fun markProcessing(id: TranscriptionId) {
        dsl.update(TRANSCRIPTIONS)
            .set(TRANSCRIPTIONS.STARTED_AT, OffsetDateTime.now())
            .set(TRANSCRIPTIONS.ATTEMPTS, TRANSCRIPTIONS.ATTEMPTS.plus(1))
            .set(TRANSCRIPTIONS.STATUS, JobStatus.processing)
            .where(TRANSCRIPTIONS.ID.eq(id.value.toJavaUuid()))
            .execute()
    }

    override fun markFailed(id: TranscriptionId, message: String) {
        dsl.update(TRANSCRIPTIONS)
            .set(TRANSCRIPTIONS.STATUS, JobStatus.failed)
            .set(TRANSCRIPTIONS.ERROR_MESSAGE, message.take(1000))
            .set(TRANSCRIPTIONS.FINISHED_AT, OffsetDateTime.now())
            .where(TRANSCRIPTIONS.ID.eq(id.value.toJavaUuid()))
            .execute()
    }

    override fun markDone(
        id: TranscriptionId,
        fullText: String,
        segments: List<NewSegment>
    ) {
        dsl.transaction { cfg ->
            val tx = DSL.using(cfg)
            tx.update(TRANSCRIPTIONS)
                .set(TRANSCRIPTIONS.STATUS, JobStatus.done)
                .set(TRANSCRIPTIONS.FULL_TEXT, fullText)
                .set(TRANSCRIPTIONS.FINISHED_AT, OffsetDateTime.now())
                .where(TRANSCRIPTIONS.ID.eq(id.value.toJavaUuid()))
                .execute()
            if (segments.isNotEmpty()) {
                tx.batchInsert(
                    segments.mapIndexed { index, s ->
                        tx.newRecord(TRANSCRIPT_SEGMENTS).apply {
                            transcriptionId = id.value.toJavaUuid()
                            seq = index
                            startMs = s.startMs
                            endMs = s.endMs
                            text = s.text
                        }
                    }
                ).execute()
            }
        }
    }

    // При старте сервера: ключей от этих задач в памяти уже нет, довести их не получится
    override fun failInterrupted() {
        dsl.update(TRANSCRIPTIONS)
            .set(TRANSCRIPTIONS.STATUS, JobStatus.failed)
            .set(TRANSCRIPTIONS.ERROR_MESSAGE, "Сервер был перезапущен, запустите расшифровку заново")
            .set(TRANSCRIPTIONS.FINISHED_AT, OffsetDateTime.now())
            .where(TRANSCRIPTIONS.STATUS.`in`(JobStatus.queued, JobStatus.processing))
            .execute()
    }
}