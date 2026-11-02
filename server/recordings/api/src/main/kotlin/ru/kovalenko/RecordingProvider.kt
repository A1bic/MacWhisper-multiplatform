package ru.kovalenko

import ru.kovalenko.dto.RecordingDto
import java.io.InputStream

interface RecordingProvider {
    fun find(recordingId: RecordingId, userId: UserId): RecordingDto?

    fun openAudio(recordingId: RecordingId): InputStream
}
