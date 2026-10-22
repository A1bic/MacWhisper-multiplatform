package ru.kovalenko

import io.ktor.server.application.Application
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTtl: Duration,
    val refreshTtl: Duration
)

fun Application.jwtConfig() = environment.config.config("jwt").let {
    JwtConfig(
        secret = it.property("secret").getString(),
        issuer = it.property("issuer").getString(),
        audience = it.property("audience").getString(),
        realm  = it.property("realm").getString(),
        accessTtl = it.property("accessTtlMinutes").getString().toInt().minutes,
        refreshTtl = it.property("refreshTtlDays").getString().toInt().days,
    )
}