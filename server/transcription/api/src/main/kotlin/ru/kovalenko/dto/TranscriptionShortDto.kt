package ru.kovalenko.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class TranscriptionShortDto(

    val id: Uuid,
    val recordingId: Uuid,
    val status: TranscriptionStatus,
    val model: String,
    val language: String?,
    val preview: String?,
    val createdAt : Instant,
    val finishedAt: Instant?,
)