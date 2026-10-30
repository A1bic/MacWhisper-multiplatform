package ru.kovalenko

import io.ktor.server.application.Application

fun Application.startBackgroundJobs(services: Services) {
    services.transcriptionRepository.failInterrupted()
    services.transcriptionWorker.start(this)
}
