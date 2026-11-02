package ru.kovalenko.services

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.kovalenko.FakeRecordingProvider
import ru.kovalenko.FakeTranscriptionRepository
import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.TranscriptionWorker
import ru.kovalenko.UserId
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.dto.TranscriptionStatus
import ru.kovalenko.mockHttpClient
import ru.kovalenko.operations.ExportTranscription
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.transcription.jooq.enums.JobStatus
import ru.kovalenko.whisper.WhisperClient
import kotlin.uuid.Uuid

class TranscriptionServiceImplTest {
    private val repository = FakeTranscriptionRepository()
    private val recordings = FakeRecordingProvider()
    private val owner = UserId(Uuid.random())
    private val stranger = UserId(Uuid.random())
    private val engine = EngineDto("http://engine.test/v1", "key", "gigaam-mlx-rnnt")
    private val scope = CoroutineScope(Job())
    private val worker = TranscriptionWorker(repository, recordings, WhisperClient(mockHttpClient {
        respond("""{"text":"Готовый текст"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
    }))
    private val service = TranscriptionServiceImpl(repository, recordings, worker)

    @AfterEach
    fun tearDown() {
        scope.cancel()
    }

    private fun stored(status: JobStatus, fullText: String? = null): TranscriptionId {
        val recordingId = recordings.add(owner)
        val entity = repository.save(NewTranscription(recordingId, engine.model, "ru"))
        repository.put(entity.copy(status = status, fullText = fullText))
        return entity.id
    }

    @Test
    fun `queue fails for unknown recording`() {
        val response = service.queueTranscription(QueueTranscription.Request(owner, RecordingId(Uuid.random()), engine, "ru"))

        assertThat(response).isEqualTo(QueueTranscription.Response.Failure("Recording not found"))
        assertThat(repository.entities).isEmpty()
    }

    @Test
    fun `queue fails for recording of another user`() {
        val recordingId = recordings.add(owner)

        val response = service.queueTranscription(QueueTranscription.Request(stranger, recordingId, engine, "ru"))

        assertThat(response).isInstanceOf(QueueTranscription.Response.Failure::class.java)
        assertThat(repository.entities).isEmpty()
    }

    @Test
    fun `queue saves transcription as queued`() {
        val recordingId = recordings.add(owner)

        val response = service.queueTranscription(QueueTranscription.Request(owner, recordingId, engine, "ru"))

        val dto = (response as QueueTranscription.Response.Success).dto
        assertThat(dto.status).isEqualTo(TranscriptionStatus.QUEUED)
        assertThat(dto.recordingId).isEqualTo(recordingId.value)
        assertThat(dto.model).isEqualTo("gigaam-mlx-rnnt")
        assertThat(dto.language).isEqualTo("ru")
        assertThat(repository.entities).containsKey(TranscriptionId(dto.id))
    }

    @Test
    fun `queued transcription is picked up by worker`() {
        worker.start(scope)
        val recordingId = recordings.add(owner)

        val dto = (service.queueTranscription(QueueTranscription.Request(owner, recordingId, engine, null))
                as QueueTranscription.Response.Success).dto
        repository.awaitFinished()

        val entity = repository.entities.getValue(TranscriptionId(dto.id))
        assertThat(entity.status).isEqualTo(JobStatus.done)
        assertThat(entity.fullText).isEqualTo("Готовый текст")
    }

    @Test
    fun `get fails for unknown transcription`() {
        val response = service.getTranscription(GetTranscription.Request(owner, TranscriptionId(Uuid.random())))

        assertThat(response).isEqualTo(GetTranscription.Response.Failure("Transcription not found"))
    }

    @Test
    fun `get hides transcription from another user`() {
        val id = stored(JobStatus.done, "Текст")

        val response = service.getTranscription(GetTranscription.Request(stranger, id))

        assertThat(response).isEqualTo(GetTranscription.Response.Failure("Transcription not found"))
    }

    @Test
    fun `get returns transcription to its owner`() {
        val id = stored(JobStatus.done, "Текст")

        val response = service.getTranscription(GetTranscription.Request(owner, id))

        val dto = (response as GetTranscription.Response.Success).dto
        assertThat(dto.id).isEqualTo(id.value)
        assertThat(dto.status).isEqualTo(TranscriptionStatus.DONE)
        assertThat(dto.fullText).isEqualTo("Текст")
    }

    @Test
    fun `export fails for unknown transcription`() {
        val response = service.exportTranscription(ExportTranscription.Request(owner, TranscriptionId(Uuid.random())))

        assertThat(response).isEqualTo(ExportTranscription.Response.Failure("Transcription not found"))
    }

    @Test
    fun `export hides transcription from another user`() {
        val id = stored(JobStatus.done, "Текст")

        val response = service.exportTranscription(ExportTranscription.Request(stranger, id))

        assertThat(response).isEqualTo(ExportTranscription.Response.Failure("Transcription not found"))
    }

    @Test
    fun `export is not ready while transcription is in progress`() {
        for (status in listOf(JobStatus.queued, JobStatus.processing, JobStatus.failed)) {
            val response = service.exportTranscription(ExportTranscription.Request(owner, stored(status)))

            assertThat(response).`as`(status.name).isInstanceOf(ExportTranscription.Response.NotReady::class.java)
        }
    }

    @Test
    fun `export returns text file of finished transcription`() {
        val id = stored(JobStatus.done, "Привет мир")

        val response = service.exportTranscription(ExportTranscription.Request(owner, id))

        assertThat(response).isEqualTo(ExportTranscription.Response.Success("transcription-${id.value}.txt", "Привет мир"))
    }
}
