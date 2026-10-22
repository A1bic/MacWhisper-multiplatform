package ru.kovalenko.operations

import ru.kovalenko.TranscriptionId
import ru.kovalenko.UserId
import ru.kovalenko.dto.TranscriptionDto

class GetTranscription private constructor() {
    data class Request(val userId: UserId,
                       val transcriptionId: TranscriptionId)

    sealed interface Response {
        data class Success(val dto: TranscriptionDto) : Response
        data class Failure(val message: String) : Response
    }
}