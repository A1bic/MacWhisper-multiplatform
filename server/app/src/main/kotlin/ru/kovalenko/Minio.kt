package ru.kovalenko

import io.ktor.server.application.Application
import io.minio.MinioClient

data class MinioConfig(val endpoint: String, val accessKey: String, val secretKey: String, val buckets: MinioBuckets)
data class MinioBuckets(val recordings: String, val transcriptions: String)

fun Application.minioConfig() = environment.config.config("minio").let {
    MinioConfig(
        endpoint = it.property("endpoint").getString(),
        accessKey = it.property("accessKey").getString(),
        secretKey = it.property("secretKey").getString(),
        buckets = MinioBuckets(
            recordings = it.property("buckets.recordings").getString(),
            transcriptions = it.property("buckets.transcriptions").getString(),
        )
    )
}

fun createMinioClient(cfg: MinioConfig): MinioClient =
    MinioClient.builder()
        .endpoint(cfg.endpoint)
        .credentials(cfg.accessKey, cfg.secretKey)
        .build()
