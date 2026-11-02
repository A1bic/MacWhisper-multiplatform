package ru.kovalenko.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TranscriptionStatus {
    @SerialName("queued") QUEUED,
    @SerialName("processing") PROCESSING,
    @SerialName("done") DONE,
    @SerialName("failed") FAILED
}