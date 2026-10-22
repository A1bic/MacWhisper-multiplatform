package ru.kovalenko.operations

import kotlinx.serialization.Serializable
import ru.kovalenko.dto.RefreshTokenDto

class RegisterUser private constructor() {
    @Serializable
    data class Request(
        val email: String,
        val password: String,
        val displayName: String? = null,
        val deviceInfo: String? = null
    )

    sealed interface Response {
        data class Success(val userDto: RefreshTokenDto) : Response
        data class Failure(val message: String) : Response
    }
}