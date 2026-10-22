package ru.kovalenko.db

import ru.kovalenko.RecordingId
import ru.kovalenko.UserId
import ru.kovalenko.repository.entity.RecordingEntity
import ru.kovalenko.repository.entity.NewRecording

interface RecordingRepository {
    fun save(rec: NewRecording): RecordingEntity
    fun findById(id: RecordingId): RecordingEntity?
    fun findById(id: RecordingId, userId: UserId): RecordingEntity?
    fun findAll(): List<RecordingEntity>
    fun delete(id: RecordingId)
}