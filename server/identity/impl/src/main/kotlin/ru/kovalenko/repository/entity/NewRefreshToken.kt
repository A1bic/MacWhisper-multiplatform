package ru.kovalenko.repository.entity

import ru.kovalenko.UserId
import kotlin.time.Instant

data class NewRefreshToken(
    val userId: UserId,
    val tokenHash: String,
    val deviceInfo: String?,
    val expiresAt: Instant
)