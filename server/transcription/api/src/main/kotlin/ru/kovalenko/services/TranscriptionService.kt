package ru.kovalenko.services

import ru.kovalenko.operations.GetAllTranscriptions
import ru.kovalenko.operations.GetTranscription
import ru.kovalenko.operations.QueueTranscription

interface TranscriptionService {
    suspend fun getTranscription(req: GetTranscription.Request): GetTranscription.Response
    suspend fun getAllTranscriptions(req: GetAllTranscriptions.Request): GetAllTranscriptions.Response
    suspend fun queueTranscription(req: QueueTranscription.Request): QueueTranscription.Response
}
