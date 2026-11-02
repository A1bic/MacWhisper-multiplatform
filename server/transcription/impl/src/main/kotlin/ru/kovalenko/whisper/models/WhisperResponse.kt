package ru.kovalenko.whisper.models

import kotlinx.serialization.Serializable

@Serializable
data class WhisperResponse(
    val text: String,
    val segments: List<WhisperSegment> = emptyList()
)

@Serializable
data class WhisperSegment(
    val start: Double,
    val end: Double,
    val text: String
)