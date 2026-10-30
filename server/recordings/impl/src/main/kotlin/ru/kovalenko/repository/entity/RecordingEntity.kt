package ru.kovalenko.repository.entity

import ru.kovalenko.RecordingId
import ru.kovalenko.dto.RecordingDto
import ru.kovalenko.dto.TranscriptionDto
import ru.kovalenko.recordings.jooq.tables.records.RecordingsRecord
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

data class RecordingEntity(
    val id: RecordingId,
    val title: String,
    val originalFileName: String?,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long?,
    val storageBucket: String,
    val storageKey: String,
    val checksumSha256: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
)

internal fun RecordingsRecord.toEntity() = RecordingEntity(
    id = RecordingId(id!!.toKotlinUuid()),
    title = title,
    originalFileName = originalFilename,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMs = durationMs,
    storageBucket = storageBucket,
    storageKey = storageKey,
    checksumSha256 = checksumSha256,
    createdAt = createdAt!!.toInstant().toKotlinInstant(),
    updatedAt = updatedAt!!.toInstant().toKotlinInstant(),
    deletedAt = deletedAt?.toInstant()?.toKotlinInstant()
)

internal fun RecordingEntity.toDto(latestTranscription: TranscriptionDto? = null) = RecordingDto(
    id = id.value,
    title = title,
    originalFileName = originalFileName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMs = durationMs,
    checksumSha256 = checksumSha256,
    lastestTranscription = latestTranscription,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt
)
