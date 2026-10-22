package ru.kovalenko.repository.entity

import ru.kovalenko.UserId
import kotlin.time.Instant

data class UserEntity(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)