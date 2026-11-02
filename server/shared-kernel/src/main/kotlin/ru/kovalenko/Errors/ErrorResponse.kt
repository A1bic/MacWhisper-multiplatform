package ru.kovalenko.Errors

data class ErrorResponse(
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String,
    val code: String,
    val errors: List<Error>
)