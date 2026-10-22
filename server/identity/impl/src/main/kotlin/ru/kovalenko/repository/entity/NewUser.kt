package ru.kovalenko.repository.entity

data class NewUser(
    val email: String,
    val password: String,
    val displayName: String?,
)
