package ru.kovalenko.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ru.kovalenko.FakeFileStorage
import ru.kovalenko.FakeRecordingRepository
import ru.kovalenko.RecordingId
import ru.kovalenko.UserId
import ru.kovalenko.operations.AddRecording
import ru.kovalenko.operations.DownloadRecording
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class RecordingServiceImplTest {
    private val storage = FakeFileStorage()
    private val repository = FakeRecordingRepository()
    private val service = RecordingServiceImpl(storage, repository)
    private val userId = UserId(Uuid.random())
    private val audio = byteArrayOf(1, 2, 3, 4, 5)

    private fun request() = AddRecording.Request(
        userId = userId,
        title = "Встреча",
        originalFilename = "meeting.m4a",
        mimeType = "audio/mp4",
        content = audio.inputStream(),
    )

    @Test
    fun `save uploads audio under user folder`() {
        val dto = (service.save(request()) as AddRecording.Response.Success).dto

        val key = "users/${userId.value}/recordings/${dto.id}"
        assertThat(storage.objects).containsOnlyKeys(key)
        assertThat(storage.objects.getValue(key)).isEqualTo(audio)
        assertThat(storage.contentTypes.getValue(key)).isEqualTo("audio/mp4")
    }

    @Test
    fun `save stores metadata from uploaded object`() {
        val dto = (service.save(request()) as AddRecording.Response.Success).dto

        val saved = repository.saved.single()
        assertThat(saved.id.value).isEqualTo(dto.id)
        assertThat(saved.userId).isEqualTo(userId)
        assertThat(saved.title).isEqualTo("Встреча")
        assertThat(saved.originalFilename).isEqualTo("meeting.m4a")
        assertThat(saved.mimeType).isEqualTo("audio/mp4")
        assertThat(saved.sizeBytes).isEqualTo(5)
        assertThat(saved.checksumSha256).isEqualTo("sha-of-${saved.storageKey}")
        assertThat(saved.storageBucket).isEqualTo("recordings")
        assertThat(saved.storageKey).isEqualTo("users/${userId.value}/recordings/${dto.id}")
    }

    @Test
    fun `save returns recording dto`() {
        val dto = (service.save(request()) as AddRecording.Response.Success).dto

        assertThat(dto.title).isEqualTo("Встреча")
        assertThat(dto.originalFileName).isEqualTo("meeting.m4a")
        assertThat(dto.mimeType).isEqualTo("audio/mp4")
        assertThat(dto.sizeBytes).isEqualTo(5)
        assertThat(dto.lastestTranscription).isNull()
        assertThat(dto.deletedAt).isNull()
    }

    @Test
    fun `save gives every recording its own id`() {
        val first = (service.save(request()) as AddRecording.Response.Success).dto
        val second = (service.save(request()) as AddRecording.Response.Success).dto

        assertThat(first.id).isNotEqualTo(second.id)
        assertThat(storage.objects).hasSize(2)
    }

    @Test
    fun `save removes uploaded audio when database save fails`() {
        repository.failOnSave = IllegalStateException("db is down")

        assertThatThrownBy { service.save(request()) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("db is down")
        assertThat(storage.objects).isEmpty()
        assertThat(storage.deleted).singleElement().asString().startsWith("users/${userId.value}/recordings/")
    }

    @Test
    fun `download returns not found for unknown recording`() {
        val response = service.download(DownloadRecording.Request(RecordingId(Uuid.random())))

        assertThat(response).isInstanceOf(DownloadRecording.Response.NotFound::class.java)
    }

    @Test
    fun `download returns temporary link for 15 minutes`() {
        val dto = (service.save(request()) as AddRecording.Response.Success).dto
        val key = repository.saved.single().storageKey

        val before = Clock.System.now()
        val response = service.download(DownloadRecording.Request(RecordingId(dto.id)))
        val after = Clock.System.now()

        val link = (response as DownloadRecording.Response.Success).response
        assertThat(link.url).isEqualTo("https://storage.test/recordings/$key?ttl=900")
        assertThat(link.mimeType).isEqualTo("audio/mp4")
        assertThat(link.sizeBytes).isEqualTo(5)
        assertThat(link.expiresAt).isBetween(before + 15.minutes, after + 15.minutes)
    }
}
