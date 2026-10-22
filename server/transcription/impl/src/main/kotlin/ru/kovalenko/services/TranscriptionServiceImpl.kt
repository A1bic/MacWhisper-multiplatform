package ru.kovalenko.services

import ru.kovalenko.RecordingProvider
import ru.kovalenko.TranscriptionJob
import ru.kovalenko.TranscriptionWorker
import ru.kovalenko.operations.GetAllTranscriptions
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription
import ru.kovalenko.repository.TranscriptionRepository
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.repository.mappers.toDto

class TranscriptionServiceImpl(
    private val transcriptions: TranscriptionRepository,
    private val recordings: RecordingProvider,
    private val worker: TranscriptionWorker,
) : TranscriptionService {

    override suspend fun queueTranscription(req: QueueTranscription.Request): QueueTranscription.Response {
        // Запись есть и принадлежит этому пользователю?
        recordings.find(req.recordingId, req.userId)
            ?: return QueueTranscription.Response.Failure("Recording not found")

        val entity = transcriptions.save(NewTranscription(req.recordingId, req.engine.model, req.language))
        worker.enqueue(TranscriptionJob(entity.id, req.recordingId, req.userId, req.engine, req.language))
        // Отвечаем сразу, со статусом queued
        return QueueTranscription.Response.Success(entity.toDto())
    }

    override suspend fun getTranscription(req: GetTranscription.Request): GetTranscription.Response {
        val entity = transcriptions.findById(req.transcriptionId)
            ?: return GetTranscription.Response.Failure("Transcription not found")
        // Чужую расшифровку не показываем
        recordings.find(entity.recordingId, req.userId)
            ?: return GetTranscription.Response.Failure("Transcription not found")
        return GetTranscription.Response.Success(entity.toDto())
    }

    override suspend fun getAllTranscriptions(req: GetAllTranscriptions.Request): GetAllTranscriptions.Response {
        TODO("Not yet implemented")
    }
}
