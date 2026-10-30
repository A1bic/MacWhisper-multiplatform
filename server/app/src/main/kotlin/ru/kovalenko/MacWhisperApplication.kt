package ru.kovalenko

import io.ktor.server.application.Application
import io.ktor.server.application.serverConfig
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val env = applicationEnvironment {
        config = ApplicationConfig("application.yaml")
    }

    embeddedServer(
        factory = Netty,
        rootConfig = serverConfig(env) {
            module(Application::macWhisperModule)
        },
        configure = {
            connector {
                port = env.config.property("ktor.deployment.port").getString().toInt()
            }
        }
    ).start(wait = true)
}

fun Application.macWhisperModule() {
    configureSerialization()
    val services = registerServices()
    configureSecurity(services.jwtConfig)
    configureRouting(services)
    startBackgroundJobs(services)
}
