# Whisper Backend

Бэкенд для Android-приложения транскрибации аудио (порт MacWhisper на Kotlin).
Распознаёт речь, делает краткое содержание с помощью LLM и синхронизирует записи между устройствами.

<!-- Бейджи: замени <user>/<repo> на свои -->
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?logo=kotlin&logoColor=white)
![License](https://img.shields.io/github/license/A1bic/MacWhisper-multiplatform)
![Build](https://img.shields.io/github/actions/workflow/status/A1bic/MacWhisper-multiplatform/build.yml)

## Возможности

- **Транскрибация** через OpenAI-совместимый API (`/audio/transcriptions`)
- **Любой провайдер**: OpenAI, Anthropic, Ollama, Qwen, vLLM, whisper.cpp и локальные модели без ключа
- **Суммаризация** расшифровок, шаблоны промптов, стриминг ответа (SSE)
- **Хранение аудио и текста** в MinIO и доступ с любого устройства
- **Аккаунты**: регистрация, вход по email или логину, JWT + refresh-токены
- **Асинхронные задачи** для длинных записей с отслеживанием прогресса

## Стек

| Слой | Технологии |
|------|------------|
| Сервер | Kotlin, Ktor |
| База данных | PostgreSQL |
| Хранилище файлов | MinIO (S3) |
| Контракт API | OpenAPI 3.1 ([`openapi.yaml`](openapi.yaml)) |
| Запуск | Docker Compose |

## Быстрый старт

### Требования

- JDK 21+
- Docker и Docker Compose

### Запуск

```bash
git clone https://github.com/<user>/<repo>.git
cd <repo>
cp .env.example .env        # заполни переменные
docker compose up -d        # поднимет PostgreSQL и MinIO
./gradlew run
```

Сервер будет доступен на `http://localhost:8080`, проверка: `GET /api/v1/health`.

### Переменные окружения

| Переменная | Описание | Пример |
|------------|----------|--------|
| `DATABASE_URL` | Подключение к PostgreSQL | `jdbc:postgresql://localhost:5432/whisper` |
| `MINIO_ENDPOINT` | Адрес MinIO | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | Доступ к MinIO | — |
| `JWT_SECRET` | Секрет для подписи токенов | — |
| `ENCRYPTION_KEY` | Ключ шифрования API-ключей провайдеров | — |

## API

Полное описание лежит в [`openapi.yaml`](openapi.yaml). Его можно открыть в [Swagger Editor](https://editor.swagger.io).

Пример транскрибации:

```bash
curl -X POST http://localhost:8080/api/v1/audio/transcriptions \
  -H "Authorization: Bearer $TOKEN" \
  -F file=@meeting.m4a \
  -F model=whisper-1 \
  -F language=ru
```

## Структура проекта

```
src/main/kotlin/
├── auth/            # регистрация, вход, токены
├── providers/       # адаптеры OpenAI / Ollama / Anthropic
├── transcription/   # распознавание речи
├── summaries/       # суммаризация
├── recordings/      # записи и работа с MinIO
└── Application.kt
```

## Лицензия

[MIT](LICENSE)