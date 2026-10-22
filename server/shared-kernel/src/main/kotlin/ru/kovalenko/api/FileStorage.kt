package ru.kovalenko.api

import java.io.InputStream
import kotlin.time.Duration

interface FileStorage {
    suspend fun ensureBucket()
    suspend fun upload(key: String, input: InputStream, contentType: String): StoredObject
    suspend fun delete(key: String)
    suspend fun exists(key: String): Boolean
    suspend fun open(key: String): InputStream
    fun downloadUrl(key: String, ttl: Duration): String
    val bucket: String
}

data class StoredObject(val key: String, val sizeBytes: Long, val sha256: String)
