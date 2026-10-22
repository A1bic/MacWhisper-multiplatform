package ru.kovalenko.repository.mappers

import ru.kovalenko.RecordingId
import ru.kovalenko.dto.RecordingDto
import ru.kovalenko.repository.entity.RecordingEntity
import ru.kovalenko.dto.TranscriptionDto
import ru.kovalenko.recordings.jooq.tables.records.RecordingsRecord
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

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