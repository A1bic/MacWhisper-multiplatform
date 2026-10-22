package ru.kovalenko

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.kovalenko.operations.CreateTranscriptionRequest
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription
import ru.kovalenko.services.TranscriptionService
import kotlin.uuid.Uuid

fun Application.configureTranscriptionRouting(service: TranscriptionService) {
    routing {
        authenticate("jwt") {
            route("/api/v1") {
                route("/recordings/{recordingId}/transcriptions") {
                    get {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@get call.respond(HttpStatusCode.BadRequest)
                    }

                    post {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val body = call.receive<CreateTranscriptionRequest>()

                        val request = QueueTranscription.Request(call.userId(), recordingId, body.engine, body.language)
                        when (val r = service.queueTranscription(request)) {
                            // 202 = «принято, делаю в фоне»
                            is QueueTranscription.Response.Success -> call.respond(HttpStatusCode.Accepted, r.dto)
                            is QueueTranscription.Response.Failure -> call.respond(HttpStatusCode.NotFound, r.message)
                        }
                    }
                }
                route("/transcriptions/{transcriptionId}") {
                    get {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@get call.respond(HttpStatusCode.BadRequest)

                        when (val r = service.getTranscription(GetTranscription.Request(call.userId(), transcriptionId))) {
                            is GetTranscription.Response.Success -> call.respond(r.dto)
                            is GetTranscription.Response.Failure -> call.respond(HttpStatusCode.NotFound, r.message)
                        }
                    }
                    patch {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    }
                    delete {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    }
                    post("/retry") {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    get("/segments") {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@get call.respond(HttpStatusCode.BadRequest)
                    }
                    get("/export") {
                        val transcriptionId = call.parameters["transcriptionId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::TranscriptionId)
                            ?: return@get call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }
        }
    }
}
