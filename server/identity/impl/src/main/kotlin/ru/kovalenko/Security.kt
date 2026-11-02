package ru.kovalenko

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity(cfg: JwtConfig) {
    install(Authentication) {
        jwt("jwt") {
            realm = cfg.realm
            verifier(
                JWT.require(Algorithm.HMAC256(cfg.secret))
                    .withIssuer(cfg.issuer)
                    .withAudience(cfg.audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized) }
        }
    }
}