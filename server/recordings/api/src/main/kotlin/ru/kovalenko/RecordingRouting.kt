package ru.kovalenko

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.jvm.javaio.toInputStream
import ru.kovalenko.operations.AddRecording
import ru.kovalenko.operations.DownloadRecording
import ru.kovalenko.service.RecordingService
import kotlin.uuid.Uuid

fun Application.configureRecordingRouting(recordingService: RecordingService) {
    routing {
        authenticate("jwt") {
            route("/api/v1/recordings") {
                get("/") {
                }
                post("/") {
                    val multipart: MultiPartData = call.receiveMultipart(formFieldLimit = 500L * 1024 * 1024)
                    var title: String? = null
                    var response : AddRecording.Response? = null

                    multipart.forEachPart { part ->
                        try {
                            when (part) {
                                is PartData.FormItem ->
                                    when (part.name) {
                                        "title" -> title = part.value
                                    }
                                is PartData.FileItem -> {
                                    response = recordingService.save(
                                        AddRecording.Request(
                                            userId = call.userId(),
                                            title = title ?: part.originalFileName ?: "Без названия",
                                            originalFilename = part.originalFileName,
                                            mimeType = part.contentType?.toString() ?: "application/octet-stream",
                                            content = part.provider().toInputStream()
                                        )
                                    )
                                }
                                else -> {}
                            }
                        }
                        finally {
                            part.release()
                        }
                    }

                    when (val r = response) {
                        null -> call.respond(HttpStatusCode.BadRequest, "Нет файла в поле формы")
                        is AddRecording.Response.Success -> call.respond(HttpStatusCode.Created, r.dto)
                        is AddRecording.Response.Failure -> call.respond(HttpStatusCode.BadRequest, r.message)
                    }

                }
                route("{recordingId}") {
                    get("/") {
                    }
                    patch("/") {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    }
                    delete("/") {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    }
                    get("/audio") {
                        val recordingId = call.parameters["recordingId"]
                            ?.let(Uuid::parseOrNull)
                            ?.let(::RecordingId)
                            ?: return@get call.respond(HttpStatusCode.BadRequest)
                        val request = DownloadRecording.Request(recordingId)

                        when (val response = recordingService.download(request)) {
                            is DownloadRecording.Response.NotFound -> call.respond(HttpStatusCode.NotFound)
                            is DownloadRecording.Response.Success -> {
                                call.respond(response.response)
                            }
                        }
                    }
                }
            }
        }
    }
}