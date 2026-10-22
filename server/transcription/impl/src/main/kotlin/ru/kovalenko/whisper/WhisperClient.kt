package ru.kovalenko.whisper

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.whisper.models.WhisperResponse

class WhisperClient(private val http: HttpClient) {
    suspend fun transcribe(
        engine: EngineDto,
        audio: ByteArray,
        fileName: String,
        mimeType: String,
        language: String?
    ) : WhisperResponse {
        val response = http.submitFormWithBinaryData(
            url = engine.baseUrl.trimEnd('/') + "/audio/transcriptions",
            formData = formData {
                append("model", engine.model)
                append("response_format", "verbose_json")
                append("timestamp_granularities[]", "segment")
                if (language != null) append("language", language)
                append("file", audio, Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                } )
            },
        ) {
            bearerAuth(engine.apiKey)
        }
        if (!response.status.isSuccess()) {
            error("Transcribe service responded ${response.status.value}: ${response.bodyAsText().take(500)} ")
        }
        return response.body()
    }
}