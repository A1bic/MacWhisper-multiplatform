package ru.kovalenko

import io.ktor.server.application.Application

fun Application.configureRouting(services: Services) {
    configureAuthRouting(services.auth)
    configureRecordingRouting(services.recordings)
    configureTranscriptionRouting(services.transcriptions)
}
