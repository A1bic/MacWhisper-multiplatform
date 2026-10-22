package ru.kovalenko

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import ru.kovalenko.dto.EngineDto
import ru.kovalenko.repository.TranscriptionRepository
import ru.kovalenko.repository.entity.NewSegment
import ru.kovalenko.whisper.WhisperClient
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToLong

data class TranscriptionJob(
    val transcriptionId: TranscriptionId,
    val recordingId: RecordingId,
    val userId: UserId,
    val engine: EngineDto,
    val language: String?,
)

class TranscriptionWorker(
    private val repository: TranscriptionRepository,
    private val audioProvider: RecordingProvider,
    private val whisperClient: WhisperClient
) {
    private val queue = Channel<TranscriptionJob>(UNLIMITED)

    fun enqueue(job: TranscriptionJob) {
        queue.trySend(job)
    }

    fun start(scope: CoroutineScope) = scope.launch(Dispatchers.IO) {
        for (job in queue) {
            process(job)
        }
    }

    private suspend fun process(job: TranscriptionJob) {
        try {
            repository.markProcessing(job.transcriptionId)

            val recording = audioProvider.find(job.recordingId, job.userId)
                ?: error("Запись не найдена")
            val bytes = audioProvider.openAudio(job.recordingId).use { it.readBytes() }

            val result = whisperClient.transcribe(
                engine = job.engine,
                audio = bytes,
                // Сервисы определяют формат по расширению файла
                fileName = recording.originalFileName ?: "audio.m4a",
                mimeType = recording.mimeType,
                language = job.language,
            )

            repository.markDone(
                id = job.transcriptionId,
                fullText = result.text.trim(),
                segments = result.segments.map {
                    NewSegment(
                        startMs = (it.start * 1000).roundToLong(),   // секунды → миллисекунды
                        endMs = (it.end * 1000).roundToLong(),
                        text = it.text.trim(),
                    )
                },
            )
        } catch (e: CancellationException) {
            throw e   // сервер останавливается — не помечаем задачу как упавшую, просто выходим
        } catch (e: Exception) {
            repository.markFailed(job.transcriptionId, e.message ?: e::class.simpleName ?: "Неизвестная ошибка")
        }
    }
}