package ru.kovalenko.operations

import kotlinx.serialization.Serializable
import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.UserId
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.dto.TranscriptionDto

@Serializable
data class CreateTranscriptionRequest(val engine: EngineDto, val language: String? = null)

class QueueTranscription private constructor() {
    data class Request(val userId: UserId,
                       val recordingId: RecordingId,
                       val engine: EngineDto,
                       val language: String?)

    sealed interface Response {
        data class Success(val dto: TranscriptionDto) : Response
        data class Failure(val message: String) : Response
    }
}