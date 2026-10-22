package ru.kovalenko.operations

import ru.kovalenko.UserId
import ru.kovalenko.dto.RecordingDto
import java.io.InputStream

class AddRecording private constructor() {
    data class Request(
        val userId: UserId,
        val title: String,
        val originalFilename: String?,
        val mimeType: String,
        val content: InputStream
    )

    sealed interface Response {
        data class Success(val dto: RecordingDto): Response
        data class Failure(val message: String): Response
    }
}