package ru.kovalenko.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class RecordingDto(
    val id: Uuid,
    val title: String,
    val originalFileName: String?,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMs: Long?,
    val checksumSha256: String?,
    val lastestTranscription: TranscriptionDto?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?
)