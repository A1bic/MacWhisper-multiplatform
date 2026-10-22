package ru.kovalenko.repository.entity

import ru.kovalenko.RecordingId
import kotlin.time.Instant

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
