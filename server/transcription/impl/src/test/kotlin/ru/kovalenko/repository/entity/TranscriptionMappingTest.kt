package ru.kovalenko.repository.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.kovalenko.RecordingId
import ru.kovalenko.TranscriptionId
import ru.kovalenko.dto.TranscriptionStatus
import ru.kovalenko.transcription.jooq.enums.JobStatus
import kotlin.time.Clock
import kotlin.uuid.Uuid

class TranscriptionMappingTest {
    @Test
    fun `every job status maps to api status`() {
        assertThat(JobStatus.queued.toDto()).isEqualTo(TranscriptionStatus.QUEUED)
        assertThat(JobStatus.processing.toDto()).isEqualTo(TranscriptionStatus.PROCESSING)
        assertThat(JobStatus.done.toDto()).isEqualTo(TranscriptionStatus.DONE)
        assertThat(JobStatus.failed.toDto()).isEqualTo(TranscriptionStatus.FAILED)
    }

    @Test
    fun `entity maps to dto field by field`() {
        val now = Clock.System.now()
        val entity = TranscriptionEntity(
            id = TranscriptionId(Uuid.random()),
            recordingId = RecordingId(Uuid.random()),
            status = JobStatus.failed,
            model = "whisper-1",
            language = "ru",
            fullText = null,
            errorMessage = "timeout",
            attempts = 2,
            createdAt = now,
            startedAt = now,
            finishedAt = now,
        )

        val dto = entity.toDto()

        assertThat(dto.id).isEqualTo(entity.id.value)
        assertThat(dto.recordingId).isEqualTo(entity.recordingId.value)
        assertThat(dto.status).isEqualTo(TranscriptionStatus.FAILED)
        assertThat(dto.model).isEqualTo("whisper-1")
        assertThat(dto.language).isEqualTo("ru")
        assertThat(dto.fullText).isNull()
        assertThat(dto.errorMessage).isEqualTo("timeout")
        assertThat(dto.attempts).isEqualTo(2)
        assertThat(dto.createdAt).isEqualTo(now)
        assertThat(dto.startedAt).isEqualTo(now)
        assertThat(dto.finishedAt).isEqualTo(now)
    }
}
