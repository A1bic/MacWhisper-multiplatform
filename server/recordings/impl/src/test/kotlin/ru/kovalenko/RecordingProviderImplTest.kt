package ru.kovalenko

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import ru.kovalenko.operations.AddRecording
import ru.kovalenko.service.RecordingServiceImpl
import kotlin.uuid.Uuid

class RecordingProviderImplTest {
    private val storage = FakeFileStorage()
    private val repository = FakeRecordingRepository()
    private val provider = RecordingProviderImpl(repository, storage)
    private val owner = UserId(Uuid.random())
    private val audio = byteArrayOf(10, 20, 30)

    private fun upload(): RecordingId {
        val request = AddRecording.Request(owner, "Звонок", "call.wav", "audio/wav", audio.inputStream())
        val dto = (RecordingServiceImpl(storage, repository).save(request) as AddRecording.Response.Success).dto
        return RecordingId(dto.id)
    }

    @Test
    fun `find returns recording of its owner`() {
        val id = upload()

        val dto = provider.find(id, owner)

        assertThat(dto).isNotNull
        assertThat(dto!!.id).isEqualTo(id.value)
        assertThat(dto.title).isEqualTo("Звонок")
        assertThat(dto.mimeType).isEqualTo("audio/wav")
    }

    @Test
    fun `find hides recording from another user`() {
        val id = upload()

        assertThat(provider.find(id, UserId(Uuid.random()))).isNull()
    }

    @Test
    fun `find returns null for unknown recording`() {
        assertThat(provider.find(RecordingId(Uuid.random()), owner)).isNull()
    }

    @Test
    fun `openAudio streams stored audio`() {
        val id = upload()

        val bytes = provider.openAudio(id).use { it.readBytes() }

        assertThat(bytes).isEqualTo(audio)
    }

    @Test
    fun `openAudio fails for unknown recording`() {
        val id = RecordingId(Uuid.random())

        assertThatThrownBy { provider.openAudio(id) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining(id.value.toString())
    }
}
