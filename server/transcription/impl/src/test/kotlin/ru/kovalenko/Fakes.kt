package ru.kovalenko

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.kovalenko.dto.RecordingDto
import ru.kovalenko.repository.TranscriptionRepository
import ru.kovalenko.repository.entity.NewSegment
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.repository.entity.TranscriptionEntity
import ru.kovalenko.transcription.jooq.enums.JobStatus
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.uuid.Uuid

class FakeTranscriptionRepository : TranscriptionRepository {
    val entities = mutableMapOf<TranscriptionId, TranscriptionEntity>()
    val segments = mutableMapOf<TranscriptionId, List<NewSegment>>()
    val statusHistory = mutableListOf<JobStatus>()
    private val finished = CountDownLatch(1)

    fun awaitFinished() {
        check(finished.await(5, TimeUnit.SECONDS)) { "Transcription did not finish in time" }
    }

    override fun save(t: NewTranscription): TranscriptionEntity {
        val entity = TranscriptionEntity(
            id = TranscriptionId(Uuid.random()),
            recordingId = t.recordingId,
            status = JobStatus.queued,
            model = t.model,
            language = t.language,
            fullText = null,
            errorMessage = null,
            attempts = 0,
            createdAt = Clock.System.now(),
            startedAt = null,
            finishedAt = null,
        )
        entities[entity.id] = entity
        return entity
    }

    override fun findById(id: TranscriptionId): TranscriptionEntity? = entities[id]

    override fun markProcessing(id: TranscriptionId) {
        update(id) { it.copy(status = JobStatus.processing, attempts = it.attempts + 1, startedAt = Clock.System.now()) }
    }

    override fun markDone(id: TranscriptionId, fullText: String, segments: List<NewSegment>) {
        this.segments[id] = segments
        update(id) { it.copy(status = JobStatus.done, fullText = fullText, finishedAt = Clock.System.now()) }
        finished.countDown()
    }

    override fun markFailed(id: TranscriptionId, message: String) {
        update(id) { it.copy(status = JobStatus.failed, errorMessage = message, finishedAt = Clock.System.now()) }
        finished.countDown()
    }

    override fun failInterrupted() {
        entities.replaceAll { _, e ->
            if (e.status == JobStatus.queued || e.status == JobStatus.processing) e.copy(status = JobStatus.failed) else e
        }
    }

    override fun findByRecordingId(recordingId: RecordingId): List<TranscriptionEntity> =
        entities.values.filter { it.recordingId == recordingId }.sortedByDescending { it.createdAt }

    fun put(entity: TranscriptionEntity) {
        entities[entity.id] = entity
    }

    private fun update(id: TranscriptionId, change: (TranscriptionEntity) -> TranscriptionEntity) {
        val updated = change(entities[id] ?: TranscriptionEntity(id, RecordingId(Uuid.random()), JobStatus.queued, "", null, null, null, 0, Clock.System.now(), null, null))
        entities[id] = updated
        statusHistory += updated.status
    }
}

class FakeRecordingProvider : RecordingProvider {
    private val recordings = mutableMapOf<RecordingId, Triple<UserId, RecordingDto, ByteArray>>()

    fun add(owner: UserId, audio: ByteArray = byteArrayOf(1, 2, 3), fileName: String? = "meeting.m4a", mimeType: String = "audio/mp4"): RecordingId {
        val id = RecordingId(Uuid.random())
        val now = Clock.System.now()
        val dto = RecordingDto(id.value, "Встреча", fileName, mimeType, audio.size.toLong(), null, null, null, now, now, null)
        recordings[id] = Triple(owner, dto, audio)
        return id
    }

    override fun find(recordingId: RecordingId, userId: UserId): RecordingDto? =
        recordings[recordingId]?.takeIf { it.first == userId }?.second

    override fun openAudio(recordingId: RecordingId): InputStream =
        recordings[recordingId]?.third?.inputStream() ?: error("Запись ${recordingId.value} не найдена")
}

fun mockHttpClient(handler: MockRequestHandler) = HttpClient(MockEngine(handler)) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
