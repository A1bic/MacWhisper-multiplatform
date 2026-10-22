package ru.kovalenko.operations

import kotlinx.serialization.Serializable
import ru.kovalenko.dto.RefreshTokenDto

class UpdateRefreshToken private constructor() {
    @Serializable
    data class Request(val refreshToken: String)

    sealed interface Response {
        data class Success(val dto: RefreshTokenDto) : Response
        data class Failure(val error: String) : Response
    }
}