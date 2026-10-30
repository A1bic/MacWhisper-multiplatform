package ru.kovalenko

import ru.kovalenko.api.FileStorage
import ru.kovalenko.db.RecordingRepository
import ru.kovalenko.dto.RecordingDto
import ru.kovalenko.repository.entity.toDto
import java.io.InputStream

class RecordingProviderImpl(
    private val repository: RecordingRepository,
    private val storage: FileStorage,
) : RecordingProvider {

    override fun find(recordingId: RecordingId, userId: UserId): RecordingDto? =
        repository.findById(recordingId, userId)?.toDto()

    override fun openAudio(recordingId: RecordingId): InputStream {
        val entity = repository.findById(recordingId) ?: error("Запись ${recordingId.value} не найдена")
        return storage.open(entity.storageKey)
    }
}
