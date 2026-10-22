package ru.kovalenko.repository.mappers

import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.dto.TranscriptionDto
import ru.kovalenko.dto.TranscriptionStatus
import ru.kovalenko.repository.entity.TranscriptionEntity
import ru.kovalenko.transcription.jooq.enums.JobStatus
import ru.kovalenko.transcription.jooq.tables.records.TranscriptionsRecord
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

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
