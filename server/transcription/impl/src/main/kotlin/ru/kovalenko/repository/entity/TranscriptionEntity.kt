package ru.kovalenko.repository.entity

import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.dto.TranscriptionDto
import ru.kovalenko.dto.TranscriptionStatus
import ru.kovalenko.transcription.jooq.enums.JobStatus
import ru.kovalenko.transcription.jooq.tables.records.TranscriptionsRecord
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

data class TranscriptionEntity(
    val id: TranscriptionId,
    val recordingId: RecordingId,
    val status: JobStatus,
    val model: String,
    val language: String?,
    val fullText: String?,
    val errorMessage: String?,
    val attempts: Int,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)

data class NewTranscription(
    val recordingId: RecordingId,
    val model: String,
    val language: String?,
)

data class NewSegment(
    val startMs: Long,
    val endMs: Long,
    val text: String
)

internal fun TranscriptionsRecord.toEntity() = TranscriptionEntity(
    id = TranscriptionId(id!!.toKotlinUuid()),
    recordingId = RecordingId(recordingId.toKotlinUuid()),
    status = status!!,
    model = model,
    language = language,
    fullText = fullText,
    errorMessage = errorMessage,
    attempts = attempts!!,
    createdAt = createdAt!!.toInstant().toKotlinInstant(),
    startedAt = startedAt?.toInstant()?.toKotlinInstant(),
    finishedAt = finishedAt?.toInstant()?.toKotlinInstant(),
)

internal fun TranscriptionEntity.toDto() = TranscriptionDto(
    id = id.value,
    recordingId = recordingId.value,
    status = status.toDto(),
    model = model,
    language = language,
    fullText = fullText,
    errorMessage = errorMessage,
    attempts = attempts,
    createdAt = createdAt,
    startedAt = startedAt,
    finishedAt = finishedAt,
)

internal fun JobStatus.toDto(): TranscriptionStatus = when (this) {
    JobStatus.queued -> TranscriptionStatus.QUEUED
    JobStatus.processing -> TranscriptionStatus.PROCESSING
    JobStatus.done -> TranscriptionStatus.DONE
    JobStatus.failed -> TranscriptionStatus.FAILED
}
