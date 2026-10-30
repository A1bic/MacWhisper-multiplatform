package ru.kovalenko.operations

import ru.kovalenko.TranscriptionId
import ru.kovalenko.UserId

class ExportTranscription private constructor() {
    data class Request(val userId: UserId,
                       val transcriptionId: TranscriptionId)

    sealed interface Response {
        data class Success(val fileName: String, val text: String) : Response
        data class NotReady(val message: String) : Response
        data class Failure(val message: String) : Response
    }
}
