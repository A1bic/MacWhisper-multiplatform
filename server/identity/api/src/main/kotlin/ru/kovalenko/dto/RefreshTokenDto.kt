package ru.kovalenko.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)