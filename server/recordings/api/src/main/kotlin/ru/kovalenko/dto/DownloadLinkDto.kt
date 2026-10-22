package ru.kovalenko.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DownloadLinkDto(
    val url: String,
    val expiresAt: Instant,
    val mimeType: String,
    val sizeBytes: Long)