package ru.kovalenko

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.application.serverConfig
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.minio.MinioClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import ru.kovalenko.identity.jooq.IdentitySchema
import ru.kovalenko.recordings.jooq.RecordingsSchema
import ru.kovalenko.repository.RecordingRepositoryImpl
import ru.kovalenko.repository.RecordingStorageImpl
import ru.kovalenko.repository.RefreshTokenRepositoryImpl
import ru.kovalenko.repository.UserRepositoryImpl
import ru.kovalenko.service.RecordingServiceImpl
import ru.kovalenko.services.AuthServiceImpl
import ru.kovalenko.services.TranscriptionServiceImpl
import ru.kovalenko.repository.TranscriptionRepositoryImpl
import ru.kovalenko.summarization.jooq.SummarizationSchema
import ru.kovalenko.transcription.jooq.TranscriptionSchema
import ru.kovalenko.whisper.WhisperClient
import javax.sql.DataSource
import kotlin.time.Duration.Companion.minutes

fun main() {
    val env = applicationEnvironment {
        config = ApplicationConfig("application.yaml")
    }

    embeddedServer(
        factory = Netty,
        rootConfig = serverConfig(env) {
            module(Application::macWhisperModule)
        },
        configure = {
            connector {
                port = env.config.property("ktor.deployment.port").getString().toInt()
            }
        }
    ).start(wait = true)
}

fun Application.macWhisperModule() {
    install (ContentNegotiation) {
        json()
    }
    val dslContext = configureDatabase()
    val userRepository = UserRepositoryImpl(dslContext)
    val refreshTokenRepository = RefreshTokenRepositoryImpl(dslContext)
    val jwtConfig = jwtConfig()
    configureSecurity(jwtConfig)
    val authService = AuthServiceImpl(
        users = userRepository,
        refreshTokens = refreshTokenRepository,
        passwords = PasswordHasher(),
        tokens = TokenService(jwtConfig),
        cfg = jwtConfig
    )
    configureAuthRouting(authService)


    val minioConfig = minioConfig()
    val minioClient = createMinioClient(minioConfig)
    val recordingRepository = RecordingRepositoryImpl(dslContext)
    val recordingStorage = RecordingStorageImpl(minioClient, minioConfig.buckets.recordings)
    runBlocking{ recordingStorage.ensureBucket() }
    val recordingService = RecordingServiceImpl(recordingStorage, recordingRepository)
    configureRecordingRouting(recordingService)

    // HTTP-клиент для запросов к сервисам расшифровки
    val httpClient = HttpClient(CIO) {
        // ContentNegotiation у клиента — свой плагин, не серверный
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10.minutes.inWholeMilliseconds
        }
    }
    monitor.subscribe(ApplicationStopped) { httpClient.close() }

    val recordingProvider = RecordingProviderImpl(recordingRepository, recordingStorage)
    val transcriptionRepository = TranscriptionRepositoryImpl(dslContext)
    transcriptionRepository.failInterrupted()   // хвосты от прошлого запуска
    val transcriptionWorker = TranscriptionWorker(transcriptionRepository, recordingProvider, WhisperClient(httpClient))
    transcriptionWorker.start(this)             // this = Application: живёт, пока работает сервер
    val transcriptionService = TranscriptionServiceImpl(transcriptionRepository, recordingProvider, transcriptionWorker)
    configureTranscriptionRouting(transcriptionService)
}

fun Application.configureDatabase() : DSLContext {
    val ds = HikariDataSource(HikariConfig().apply {
        jdbcUrl = environment.config.property("db.url").getString()
        username = environment.config.property("db.user").getString()
        password = environment.config.property("db.password").getString()
    })
    monitor.subscribe(ApplicationStopped) {
        ds.close()
    }

    listOf(
        IdentitySchema.IDENTITY_SCHEMA,
        RecordingsSchema.RECORDINGS_SCHEMA,
        TranscriptionSchema.TRANSCRIPTION_SCHEMA,
        SummarizationSchema.SUMMARIZATION_SCHEMA,
    ).forEach {
        schema -> migrate(ds, schema.name)
    }

    return DSL.using(ds, SQLDialect.POSTGRES)
}

fun migrate(dataSource: DataSource, schema: String) {
    Flyway.configure()
        .dataSource(dataSource)
        .schemas(schema)
        .locations("classpath:db/migration/$schema")
        .load()
        .migrate()
}

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

fun createMinioClient(cfg: MinioConfig) : MinioClient =
    MinioClient.builder()
        .endpoint(cfg.endpoint)
        .credentials(cfg.accessKey, cfg.secretKey)
        .build()