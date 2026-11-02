package ru.kovalenko.repository.entity

import ru.kovalenko.RecordingId
import ru.kovalenko.UserId

data class NewRecording(
    val id: RecordingId,
    val userId: UserId,
    val title: String,
    val originalFilename: String?,
    val mimeType: String,
    val sizeBytes: Long,
    val checksumSha256: String,
    val storageBucket: String,
    val storageKey: String
)