package ru.kovalenko.repository

import ru.kovalenko.TranscriptionId
import ru.kovalenko.repository.entity.NewSegment
import ru.kovalenko.repository.entity.NewTranscription
import ru.kovalenko.repository.entity.TranscriptionEntity

interface TranscriptionRepository {
    fun save(t: NewTranscription) : TranscriptionEntity
    fun findById(id: TranscriptionId): TranscriptionEntity?
    fun markProcessing(id: TranscriptionId)
    fun markDone(id: TranscriptionId, fullText: String, segments: List<NewSegment>)
    fun markFailed(id: TranscriptionId, message: String)
    fun failInterrupted()
}