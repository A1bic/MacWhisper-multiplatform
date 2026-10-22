package ru.kovalenko.repository

import org.jooq.DSLContext
import ru.kovalenko.RecordingId
import ru.kovalenko.UserId
import ru.kovalenko.repository.entity.NewRecording
import ru.kovalenko.db.RecordingRepository
import ru.kovalenko.repository.entity.RecordingEntity
import ru.kovalenko.recordings.jooq.tables.references.RECORDINGS
import ru.kovalenko.repository.mappers.toEntity
import kotlin.uuid.toJavaUuid

class RecordingRepositoryImpl(private val dsl: DSLContext) : RecordingRepository {
    override fun save(rec: NewRecording): RecordingEntity =
        dsl.insertInto(RECORDINGS)
            .set(RECORDINGS.ID, rec.id.value.toJavaUuid())
            .set(RECORDINGS.USER_ID, rec.userId.value.toJavaUuid())
            .set(RECORDINGS.TITLE, rec.title)
            .set(RECORDINGS.ORIGINAL_FILENAME, rec.originalFilename)
            .set(RECORDINGS.MIME_TYPE, rec.mimeType)
            .set(RECORDINGS.SIZE_BYTES, rec.sizeBytes)
            .set(RECORDINGS.CHECKSUM_SHA256, rec.checksumSha256)
            .set(RECORDINGS.STORAGE_BUCKET, rec.storageBucket)
            .set(RECORDINGS.STORAGE_KEY, rec.storageKey)
            .returning()
            .fetchSingle()
            .toEntity()

    override fun findById(id: RecordingId): RecordingEntity? =
        dsl.selectFrom(RECORDINGS)
            .where(RECORDINGS.ID.eq(id.value.toJavaUuid()))
            .fetchOne()
            ?.toEntity()

    override fun findById(id: RecordingId, userId: UserId): RecordingEntity? =
        dsl.selectFrom(RECORDINGS)
            .where(RECORDINGS.ID.eq(id.value.toJavaUuid()))
            .and(RECORDINGS.USER_ID.eq(userId.value.toJavaUuid()))
            .and(RECORDINGS.DELETED_AT.isNull)
            .fetchOne()
            ?.toEntity()

    override fun findAll(): List<RecordingEntity> =
        dsl.selectFrom(RECORDINGS)
            .fetch()
            .map { it.toEntity() }

    override fun delete(id: RecordingId) {
        dsl.deleteFrom(RECORDINGS)
            .where(RECORDINGS.ID.eq(id.value.toJavaUuid()))
            .execute()
    }
}