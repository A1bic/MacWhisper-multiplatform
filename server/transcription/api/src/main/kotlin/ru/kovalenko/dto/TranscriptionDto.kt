package ru.kovalenko.dto

import kotlinx.serialization.Serializable
import ru.kovalenko.RecordingId
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class TranscriptionDto(
    val id: Uuid,
    val recordingId: Uuid,
    val status: TranscriptionStatus,
    val model: String,
    val language: String?,
    val fullText: String?,
    val errorMessage: String?,
    val attempts: Int,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
)
