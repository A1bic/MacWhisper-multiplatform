package ru.kovalenko.api

import java.io.InputStream
import kotlin.time.Duration

interface FileStorage {
    fun ensureBucket()
    fun upload(key: String, input: InputStream, contentType: String): StoredObject
    fun delete(key: String)
    fun exists(key: String): Boolean
    fun open(key: String): InputStream
    fun downloadUrl(key: String, ttl: Duration): String
    val bucket: String
}

data class StoredObject(val key: String, val sizeBytes: Long, val sha256: String)
