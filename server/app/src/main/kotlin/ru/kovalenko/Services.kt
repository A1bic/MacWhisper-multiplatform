package ru.kovalenko

import io.ktor.server.application.Application
import ru.kovalenko.repository.RecordingRepositoryImpl
import ru.kovalenko.repository.RecordingStorageImpl
import ru.kovalenko.repository.RefreshTokenRepositoryImpl
import ru.kovalenko.repository.TranscriptionRepository
import ru.kovalenko.repository.TranscriptionRepositoryImpl
import ru.kovalenko.repository.UserRepositoryImpl
import ru.kovalenko.service.RecordingService
import ru.kovalenko.service.RecordingServiceImpl
import ru.kovalenko.services.AuthService
import ru.kovalenko.services.AuthServiceImpl
import ru.kovalenko.services.TranscriptionService
import ru.kovalenko.services.TranscriptionServiceImpl
import ru.kovalenko.whisper.WhisperClient

class Services(
    val jwtConfig: JwtConfig,
    val auth: AuthService,
    val recordings: RecordingService,
    val transcriptions: TranscriptionService,
    val transcriptionRepository: TranscriptionRepository,
    val transcriptionWorker: TranscriptionWorker,
)

fun Application.registerServices(): Services {
    val dsl = configureDatabase()

    val jwtConfig = jwtConfig()
    val authService = AuthServiceImpl(
        users = UserRepositoryImpl(dsl),
        refreshTokens = RefreshTokenRepositoryImpl(dsl),
        passwords = PasswordHasher(),
        tokens = TokenService(jwtConfig),
        cfg = jwtConfig,
    )

    val minioConfig = minioConfig()
    val recordingRepository = RecordingRepositoryImpl(dsl)
    val recordingStorage = RecordingStorageImpl(createMinioClient(minioConfig), minioConfig.buckets.recordings)
    recordingStorage.ensureBucket()
    val recordingService = RecordingServiceImpl(recordingStorage, recordingRepository)
    val recordingProvider = RecordingProviderImpl(recordingRepository, recordingStorage)

    val transcriptionRepository = TranscriptionRepositoryImpl(dsl)
    val transcriptionWorker = TranscriptionWorker(transcriptionRepository, recordingProvider, WhisperClient(configureHttpClient()))
    val transcriptionService = TranscriptionServiceImpl(transcriptionRepository, recordingProvider, transcriptionWorker)

    return Services(
        jwtConfig = jwtConfig,
        auth = authService,
        recordings = recordingService,
        transcriptions = transcriptionService,
        transcriptionRepository = transcriptionRepository,
        transcriptionWorker = transcriptionWorker,
    )
}
