package ru.kovalenko.operations

import ru.kovalenko.RecordingId
import ru.kovalenko.UserId
import ru.kovalenko.dto.TranscriptionDto

class GetAllTranscriptions {
    data class Request(val recordingId: RecordingId, val userId: UserId)

    sealed interface Response {
        data class Success(val dtos: List<TranscriptionDto>) : Response
        data class Failure(val message: String) : Response
    }
}