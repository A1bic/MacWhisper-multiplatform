package ru.kovalenko.operations

import ru.kovalenko.dto.RecordingDto

class RenameRecording private constructor() {
    data class Request(val title: String)

    sealed interface Response {
        data class Success(val recording: RecordingDto): Response
        data class Failure(val message: String): Response
    }
}
