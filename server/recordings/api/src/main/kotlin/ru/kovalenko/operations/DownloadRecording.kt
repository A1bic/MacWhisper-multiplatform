package ru.kovalenko.operations

import ru.kovalenko.RecordingId
import ru.kovalenko.dto.DownloadLinkDto
import kotlin.time.Instant

class DownloadRecording private constructor() {
    data class Request(val id: RecordingId)

    sealed interface Response {
//        data class Success(val url: String,
//                           val mimeType: String,
//                           val sizeBytes: Long,
//                           val writeTo: suspend (OutputStream) -> Unit): Response
        data class Success(val response: DownloadLinkDto) : Response
        data class NotFound(val error: String): Response
//        data class Failure(val message: String): Response
    }
}