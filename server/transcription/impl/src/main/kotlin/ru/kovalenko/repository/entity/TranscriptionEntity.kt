package ru.kovalenko.repository.entity

import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.transcription.jooq.enums.JobStatus
import kotlin.time.Instant

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