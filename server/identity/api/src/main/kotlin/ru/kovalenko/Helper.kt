package ru.kovalenko

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import kotlin.uuid.Uuid

fun ApplicationCall.userId(): UserId =
    UserId(Uuid.parse(principal<JWTPrincipal>()!!.subject!!))