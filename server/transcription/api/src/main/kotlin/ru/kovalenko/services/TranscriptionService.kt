package ru.kovalenko.services

import ru.kovalenko.operations.ExportTranscription
import ru.kovalenko.operations.GetAllTranscriptions
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription

interface TranscriptionService {
    fun getTranscription(req: GetTranscription.Request): GetTranscription.Response
    fun getAllTranscriptions(req: GetAllTranscriptions.Request): GetAllTranscriptions.Response
    fun queueTranscription(req: QueueTranscription.Request): QueueTranscription.Response
    fun exportTranscription(req: ExportTranscription.Request): ExportTranscription.Response
}
