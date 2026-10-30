package ru.kovalenko

import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.repository.entity.NewSegment
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.transcription.jooq.enums.JobStatus
import ru.kovalenko.whisper.WhisperClient
import kotlin.uuid.Uuid

class TranscriptionWorkerTest {
    private val repository = FakeTranscriptionRepository()
    private val recordings = FakeRecordingProvider()
    private val owner = UserId(Uuid.random())
    private val engine = EngineDto("http://engine.test/v1", "key", "gigaam-mlx-rnnt")
    private val scope = CoroutineScope(Job())
    private var sentBody = ""

    @AfterEach
    fun tearDown() {
        scope.cancel()
    }

    private fun worker(status: HttpStatusCode = HttpStatusCode.OK, response: String = RESPONSE) =
        TranscriptionWorker(repository, recordings, WhisperClient(mockHttpClient { req ->
            sentBody = req.body.toByteArray().decodeToString()
            respond(response, status, headersOf(HttpHeaders.ContentType, "application/json"))
        })).also { it.start(scope) }

    private fun run(worker: TranscriptionWorker, recordingId: RecordingId, userId: UserId = owner): TranscriptionId {
        val id = repository.save(NewTranscription(recordingId, engine.model, "ru")).id
        worker.enqueue(TranscriptionJob(id, recordingId, userId, engine, "ru"))
        repository.awaitFinished()
        return id
    }

    @Test
    fun `stores trimmed text and segments in milliseconds`() {
        val id = run(worker(), recordings.add(owner))

        val entity = repository.entities.getValue(id)
        assertThat(entity.status).isEqualTo(JobStatus.done)
        assertThat(entity.fullText).isEqualTo("Привет мир")
        assertThat(repository.segments.getValue(id)).containsExactly(
            NewSegment(0, 1250, "Привет"),
            NewSegment(1250, 2500, "мир"),
        )
    }

    @Test
    fun `marks job as processing before it is done`() {
        run(worker(), recordings.add(owner))

        assertThat(repository.statusHistory).containsExactly(JobStatus.processing, JobStatus.done)
    }

    @Test
    fun `sends recording file name and mime type to the engine`() {
        run(worker(), recordings.add(owner, fileName = "call.wav", mimeType = "audio/wav"))

        assertThat(sentBody).contains("call.wav").contains("audio/wav")
    }

    @Test
    fun `uses default file name when recording has none`() {
        run(worker(), recordings.add(owner, fileName = null))

        assertThat(sentBody).contains("audio.m4a")
    }

    @Test
    fun `marks job as failed when engine responds with error`() {
        val id = run(worker(HttpStatusCode.InternalServerError, "boom"), recordings.add(owner))

        val entity = repository.entities.getValue(id)
        assertThat(entity.status).isEqualTo(JobStatus.failed)
        assertThat(entity.errorMessage).contains("500").contains("boom")
        assertThat(repository.segments).isEmpty()
    }

    @Test
    fun `marks job as failed when recording belongs to another user`() {
        val id = run(worker(), recordings.add(owner), userId = UserId(Uuid.random()))

        val entity = repository.entities.getValue(id)
        assertThat(entity.status).isEqualTo(JobStatus.failed)
        assertThat(entity.errorMessage).isEqualTo("Запись не найдена")
    }

    companion object {
        const val RESPONSE = """{"text":"  Привет мир  ","segments":[{"start":0.0,"end":1.25,"text":" Привет "},{"start":1.25,"end":2.5,"text":" мир"}]}"""
    }
}
