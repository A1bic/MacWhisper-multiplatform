package ru.kovalenko

import ru.kovalenko.api.FileStorage
import ru.kovalenko.api.StoredObject
import ru.kovalenko.db.RecordingRepository
import ru.kovalenko.repository.entity.NewRecording
import ru.kovalenko.repository.entity.RecordingEntity
import java.io.InputStream
import kotlin.time.Clock
import kotlin.time.Duration

class FakeFileStorage(override val bucket: String = "recordings") : FileStorage {
    val objects = mutableMapOf<String, ByteArray>()
    val contentTypes = mutableMapOf<String, String>()
    val deleted = mutableListOf<String>()

    override fun ensureBucket() {}

    override fun upload(key: String, input: InputStream, contentType: String): StoredObject {
        val bytes = input.readBytes()
        objects[key] = bytes
        contentTypes[key] = contentType
        return StoredObject(key, bytes.size.toLong(), "sha-of-$key")
    }

    override fun delete(key: String) {
        objects.remove(key)
        deleted += key
    }

    override fun exists(key: String): Boolean = key in objects

    override fun open(key: String): InputStream = objects.getValue(key).inputStream()

    override fun downloadUrl(key: String, ttl: Duration): String =
        "https://storage.test/$bucket/$key?ttl=${ttl.inWholeSeconds}"
}

class FakeRecordingRepository : RecordingRepository {
    val saved = mutableListOf<NewRecording>()
    var failOnSave: Exception? = null
    private val entities = mutableMapOf<RecordingId, Pair<UserId, RecordingEntity>>()

    override fun save(rec: NewRecording): RecordingEntity {
        failOnSave?.let { throw it }
        val now = Clock.System.now()
        val entity = RecordingEntity(
            id = rec.id,
            title = rec.title,
            originalFileName = rec.originalFilename,
            mimeType = rec.mimeType,
            sizeBytes = rec.sizeBytes,
            durationMs = null,
            storageBucket = rec.storageBucket,
            storageKey = rec.storageKey,
            checksumSha256 = rec.checksumSha256,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
        saved += rec
        entities[rec.id] = rec.userId to entity
        return entity
    }

    override fun findById(id: RecordingId): RecordingEntity? = entities[id]?.second

    override fun findById(id: RecordingId, userId: UserId): RecordingEntity? =
        entities[id]?.takeIf { it.first == userId }?.second

    override fun findAll(): List<RecordingEntity> = entities.values.map { it.second }

    override fun delete(id: RecordingId) {
        entities.remove(id)
    }
}
