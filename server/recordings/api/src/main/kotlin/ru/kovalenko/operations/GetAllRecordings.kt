package ru.kovalenko.operations

import ru.kovalenko.dto.RecordingDto
import kotlin.time.Instant

class GetAllRecordings private constructor() {
    data class Request(val cursor: String?, val limit: Int = 20, val updatedSince: Instant, val q: String?, val sort: String = "-createdAt")

    sealed interface Response {
        data class Success(val recordings: List<RecordingDto>) : Response
        data class Failure(val message: String) : Response
    }
}