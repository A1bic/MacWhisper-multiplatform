package ru.kovalenko

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.kovalenko.operations.CreateTranscriptionRequest
import ru.kovalenko.operations.ExportTranscription
import ru.kovalenko.operations.GetAllTranscriptions
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

                        val request = GetAllTranscriptions.Request(recordingId, call.userId())

                        when (val r = service.getAllTranscriptions(request)) {
                            is GetAllTranscriptions.Response.Success -> call.respond(HttpStatusCode.OK, r.dtos)
                            is GetAllTranscriptions.Response.Failure -> call.respond(HttpStatusCode.NotFound, r.message)
                        }
                    }

                    post {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@post call.respond(HttpStatusCode.BadRequest)
                        val body = call.receive<CreateTranscriptionRequest>()

                        val request = QueueTranscription.Request(call.userId(), recordingId, body.engine, body.language)
                        when (val r = service.queueTranscription(request)) {
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
                            is GetTranscription.Response.Success -> call.respond(HttpStatusCode.OK, r.dto)
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

                        when (val r = service.exportTranscription(ExportTranscription.Request(call.userId(), transcriptionId))) {
                            is ExportTranscription.Response.Success -> {
                                call.response.header(
                                    HttpHeaders.ContentDisposition,
                                    ContentDisposition.Attachment
                                        .withParameter(ContentDisposition.Parameters.FileName, r.fileName)
                                        .toString()
                                )
                                call.respondText(r.text, ContentType.Text.Plain.withCharset(Charsets.UTF_8))
                            }
                            is ExportTranscription.Response.NotReady -> call.respond(HttpStatusCode.Conflict, r.message)
                            is ExportTranscription.Response.Failure -> call.respond(HttpStatusCode.NotFound, r.message)
                        }
                    }
                }
            }
        }
    }
}
