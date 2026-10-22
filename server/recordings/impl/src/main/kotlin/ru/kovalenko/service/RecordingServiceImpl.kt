package ru.kovalenko.service

import ru.kovalenko.RecordingId
import ru.kovalenko.api.FileStorage
import ru.kovalenko.repository.entity.NewRecording
import ru.kovalenko.db.RecordingRepository
import ru.kovalenko.dto.DownloadLinkDto
import ru.kovalenko.operations.AddRecording
import ru.kovalenko.operations.DeleteRecording
import ru.kovalenko.operations.DownloadRecording
import ru.kovalenko.operations.GetAllRecordings
import ru.kovalenko.operations.RenameRecording
import ru.kovalenko.repository.mappers.toDto
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class RecordingServiceImpl(private val recordingStorage: FileStorage,
                           private val recordingRepository: RecordingRepository) : RecordingService {

    override suspend fun save(request: AddRecording.Request): AddRecording.Response {
        val recordingId = RecordingId(Uuid.random())
        val key = "users/${request.userId.value}/recordings/${recordingId.value}"

        val stored = recordingStorage.upload(key, request.content, request.mimeType)

        val saved = try {
            recordingRepository.save(
                NewRecording(
                    id = recordingId,
                    userId = request.userId,
                    title = request.title,
                    originalFilename = request.originalFilename,
                    mimeType = request.mimeType,
                    sizeBytes = stored.sizeBytes,
                    checksumSha256 = stored.sha256,
                    storageBucket = recordingStorage.bucket,
                    storageKey = key
                )
            )
        } catch(e: Exception) {
            recordingStorage.delete(key)
            throw e
        }

        return AddRecording.Response.Success(saved.toDto())
    }

    override fun delete(request: DeleteRecording.Request): DeleteRecording.Response {
        TODO("Not yet implemented")
    }

    override fun getAll(request: GetAllRecordings.Request): GetAllRecordings.Response {
        TODO("Not yet implemented")
    }

    override fun rename(request: RenameRecording.Request): RenameRecording.Response {
        TODO("Not yet implemented")
    }

    override fun download(request: DownloadRecording.Request): DownloadRecording.Response {
        val rec = recordingRepository.findById(request.id) ?: return DownloadRecording.Response.NotFound("Recording not found")

        val ttl = 15.minutes
        val downloadLinkDto = DownloadLinkDto(
            url = recordingStorage.downloadUrl(rec.storageKey, ttl),
            expiresAt = Clock.System.now() + ttl,
            mimeType = rec.mimeType,
            sizeBytes = rec.sizeBytes
        )
        return DownloadRecording.Response.Success(downloadLinkDto)
    }
}