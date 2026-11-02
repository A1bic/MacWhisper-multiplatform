package ru.kovalenko.whisper

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.mockHttpClient
import ru.kovalenko.whisper.models.WhisperSegment

class WhisperClientTest {
    private val engine = EngineDto(baseUrl = "http://engine.test/v1/", apiKey = "secret-key", model = "gigaam-mlx-rnnt")
    private var request: HttpRequestData? = null
    private var body: String = ""

    private fun client(status: HttpStatusCode = HttpStatusCode.OK, response: String = RESPONSE) =
        WhisperClient(mockHttpClient { req ->
            request = req
            body = req.body.toByteArray().decodeToString()
            respond(response, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })

    private fun transcribe(client: WhisperClient, language: String? = "ru") =
        client.transcribe(engine, byteArrayOf(1, 2, 3), "meeting.m4a", "audio/mp4", language)

    @Test
    fun `posts to audio transcriptions under base url`() {
        transcribe(client())

        assertThat(request!!.method).isEqualTo(HttpMethod.Post)
        assertThat(request!!.url.toString()).isEqualTo("http://engine.test/v1/audio/transcriptions")
    }

    @Test
    fun `sends api key as bearer token`() {
        transcribe(client())

        assertThat(request!!.headers[HttpHeaders.Authorization]).isEqualTo("Bearer secret-key")
    }

    @Test
    fun `sends model, format, language and file in multipart form`() {
        transcribe(client())

        assertThat(request!!.body.contentType.toString()).startsWith("multipart/form-data")
        assertThat(body)
            .contains("gigaam-mlx-rnnt")
            .contains("verbose_json")
            .contains("timestamp_granularities[]")
            .contains("language")
            .contains("meeting.m4a")
            .contains("audio/mp4")
    }

    @Test
    fun `omits language when it is not set`() {
        transcribe(client(), language = null)

        assertThat(body).doesNotContain("language")
    }

    @Test
    fun `parses text and segments from response`() {
        val result = transcribe(client())

        assertThat(result.text).isEqualTo(" Привет мир ")
        assertThat(result.segments).containsExactly(
            WhisperSegment(0.0, 1.25, " Привет"),
            WhisperSegment(1.25, 2.5, " мир "),
        )
    }

    @Test
    fun `accepts response without segments`() {
        val result = transcribe(client(response = """{"text":"Привет"}"""))

        assertThat(result.text).isEqualTo("Привет")
        assertThat(result.segments).isEmpty()
    }

    @Test
    fun `fails with status and body when service responds with error`() {
        val client = client(HttpStatusCode.ServiceUnavailable, """{"error":"model is loading"}""")

        assertThatThrownBy { transcribe(client) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("503")
            .hasMessageContaining("model is loading")
    }

    companion object {
        const val RESPONSE = """{"text":" Привет мир ","language":"ru","segments":[{"id":0,"start":0.0,"end":1.25,"text":" Привет"},{"id":1,"start":1.25,"end":2.5,"text":" мир "}]}"""
    }
}
