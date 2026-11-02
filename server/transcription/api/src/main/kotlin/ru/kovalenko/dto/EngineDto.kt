package ru.kovalenko.dto

import kotlinx.serialization.Serializable

@Serializable
data class EngineDto(
    val baseUrl: String,
    val apiKey: String,
    val model: String
) {
    override fun toString(): String = "EngineDto(baseUrl=$baseUrl, model=$model, apiKey=***)"
}