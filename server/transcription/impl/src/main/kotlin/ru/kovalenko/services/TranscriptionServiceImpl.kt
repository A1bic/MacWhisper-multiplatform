package ru.kovalenko.services

import ru.kovalenko.RecordingProvider
import ru.kovalenko.TranscriptionJob
import ru.kovalenko.TranscriptionWorker
import ru.kovalenko.operations.ExportTranscription
import ru.kovalenko.operations.GetAllTranscriptions
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription
import ru.kovalenko.repository.TranscriptionRepository
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.repository.entity.toDto
import ru.kovalenko.transcription.jooq.enums.JobStatus

class TranscriptionServiceImpl(
    private val transcriptions: TranscriptionRepository,
    private val recordings: RecordingProvider,
    private val worker: TranscriptionWorker,
) : TranscriptionService {

    override fun queueTranscription(req: QueueTranscription.Request): QueueTranscription.Response {
        recordings.find(req.recordingId, req.userId)
            ?: return QueueTranscription.Response.Failure("Recording not found")

        val entity = transcriptions.save(NewTranscription(req.recordingId, req.engine.model, req.language))
        worker.enqueue(TranscriptionJob(entity.id, req.recordingId, req.userId, req.engine, req.language))
        return QueueTranscription.Response.Success(entity.toDto())
    }

    override fun getTranscription(req: GetTranscription.Request): GetTranscription.Response {
        val entity = transcriptions.findById(req.transcriptionId)
            ?: return GetTranscription.Response.Failure("Transcription not found")
        recordings.find(entity.recordingId, req.userId)
            ?: return GetTranscription.Response.Failure("Transcription not found")
        return GetTranscription.Response.Success(entity.toDto())
    }

    override fun exportTranscription(req: ExportTranscription.Request): ExportTranscription.Response {
        val entity = transcriptions.findById(req.transcriptionId)
            ?: return ExportTranscription.Response.Failure("Transcription not found")
        recordings.find(entity.recordingId, req.userId)
            ?: return ExportTranscription.Response.Failure("Transcription not found")
        if (entity.status != JobStatus.done || entity.fullText == null) {
            return ExportTranscription.Response.NotReady("Transcription is not done yet")
        }
        return ExportTranscription.Response.Success("transcription-${entity.id.value}.txt", entity.fullText)
    }

    override fun getAllTranscriptions(req: GetAllTranscriptions.Request): GetAllTranscriptions.Response {
        recordings.find(req.recordingId, req.userId)
            ?: return GetAllTranscriptions.Response.Failure("Recording not found")

        val entities = transcriptions.findByRecordingId(req.recordingId)
        return GetAllTranscriptions.Response.Success(entities.map { it.toDto() })
    }
}
