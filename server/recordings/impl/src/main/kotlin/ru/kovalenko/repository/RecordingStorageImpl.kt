package ru.kovalenko.repository

import com.google.common.io.CountingInputStream
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.Http
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.StatObjectArgs
import io.minio.errors.ErrorResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kovalenko.api.FileStorage
import ru.kovalenko.api.StoredObject
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class RecordingStorageImpl(private val client: MinioClient, override val bucket: String) : FileStorage {
    override suspend fun ensureBucket() = withContext(Dispatchers.IO) {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists)
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
    }

    override suspend fun upload(key: String, input: InputStream, contentType: String): StoredObject = withContext(Dispatchers.IO) {
            val digest = MessageDigest.getInstance("SHA-256")
            val counting = CountingInputStream(DigestInputStream(input, digest))
            client.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(key)
                    .stream(counting, -1, 10L * 1024 * 1024)
                    .contentType(contentType)
                    .build()
            )
            StoredObject(key, counting.count, digest.digest().toHexString())
        }

    override suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())
    }

    override suspend fun exists(key: String) : Boolean = withContext(Dispatchers.IO) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).`object`(key).build())
            true;
        } catch (e : ErrorResponseException) {
            if (e.errorResponse().code() == "NoSuchKey") false else throw e
        }
    }

    override suspend fun open(key: String): InputStream = withContext(Dispatchers.IO) {
        client.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(key)
                .build()
        )
    }

    override fun downloadUrl(key: String, ttl: Duration): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Http.Method.GET)
                .bucket(bucket)
                .`object`(key)
                .expiry(ttl.inWholeSeconds.toInt(), TimeUnit.SECONDS)
                .build()
        )
}