package ru.kovalenko.operations

import kotlinx.serialization.Serializable
import ru.kovalenko.dto.RefreshTokenDto

class LoginUser private constructor() {
    @Serializable
    data class Request(val email: String,
                       val password: String,
                       val deviceInfo: String? = null)

    sealed interface Response {
        data class Success(val dto: RefreshTokenDto) : Response
        data class Failure(val message: String) : Response
    }
}