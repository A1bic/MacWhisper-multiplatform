package ru.kovalenko.operations

import ru.kovalenko.RecordingId
import ru.kovalenko.dto.RecordingDto

class DeleteRecording private constructor() {
    data class Request(val id: RecordingId)
    sealed interface Response {
        data class Success(val dto: RecordingDto): Response
        data class Failure(val message: String): Response
    }
}