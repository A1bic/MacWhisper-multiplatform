package ru.kovalenko

import ru.kovalenko.dto.RecordingDto
import java.io.InputStream

interface RecordingProvider {
    // Запись пользователя или null, если её нет или она чужая
    suspend fun find(recordingId: RecordingId, userId: UserId): RecordingDto?

    // Открыть аудиофайл записи. Вызывать только после успешного find.
    suspend fun openAudio(recordingId: RecordingId): InputStream
}
