package ru.kovalenko

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

fun Application.configureHttpClient(): HttpClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10.minutes.inWholeMilliseconds
        }
    }
    monitor.subscribe(ApplicationStopped) { client.close() }
    return client
}
