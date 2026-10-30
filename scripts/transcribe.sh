#!/usr/bin/env bash

set -euo pipefail

API_URL="${API_URL:-http://localhost:8080/api/v1}"
EMAIL="${EMAIL:-demo@test.ru}"
PASSWORD="${PASSWORD:-password123}"
ENGINE_URL="${ENGINE_URL:-http://192.168.88.182:8765/v1}"
ENGINE_KEY="${ENGINE_KEY:-none}"
ENGINE_MODEL="${ENGINE_MODEL:-gigaam-mlx-rnnt}"
LANGUAGE="${LANGUAGE-ru}"
TIMEOUT="${TIMEOUT:-300}"
DOWNLOAD="${DOWNLOAD:-}"

FILE="${1:-}"
if [[ -z "$FILE" ]]; then
    echo "Использование: $0 <путь-к-аудио>" >&2
    exit 1
fi
if [[ ! -f "$FILE" ]]; then
    echo "Файл не найден: $FILE" >&2
    exit 1
fi
command -v jq >/dev/null || { echo "Нужен jq: brew install jq" >&2; exit 1; }

TITLE="${TITLE:-$(basename "$FILE")}"

case "${FILE##*.}" in
    m4a|mp4) MIME="audio/mp4" ;;
    mp3)     MIME="audio/mpeg" ;;
    wav)     MIME="audio/wav" ;;
    ogg|opus) MIME="audio/ogg" ;;
    flac)    MIME="audio/flac" ;;
    webm)    MIME="audio/webm" ;;
    *)       MIME="application/octet-stream" ;;
esac

request() {
    local out
    out=$(curl -s -w $'\n%{http_code}' "$@")
    CODE="${out##*$'\n'}"
    BODY="${out%$'\n'*}"
}

fail() {
    echo "✗ $1 (HTTP $CODE)" >&2
    [[ -n "$BODY" ]] && echo "$BODY" >&2
    exit 1
}

CREDENTIALS=$(jq -n --arg e "$EMAIL" --arg p "$PASSWORD" '{email: $e, password: $p}')

request -X POST "$API_URL/auth/register" -H "Content-Type: application/json" -d "$CREDENTIALS"
if [[ "$CODE" == "201" ]]; then
    echo "✓ Зарегистрирован $EMAIL"
else
    request -X POST "$API_URL/auth/login" -H "Content-Type: application/json" -d "$CREDENTIALS"
    [[ "$CODE" == "200" ]] || fail "Не удалось ни зарегистрироваться, ни войти как $EMAIL"
    echo "✓ Вошёл как $EMAIL"
fi
TOKEN=$(echo "$BODY" | jq -r .accessToken)
AUTH="Authorization: Bearer $TOKEN"

request -X POST "$API_URL/recordings/" -H "$AUTH" \
    -F "title=$TITLE" \
    -F "file=@$FILE;type=$MIME"
[[ "$CODE" == "201" ]] || fail "Не удалось загрузить файл"
REC_ID=$(echo "$BODY" | jq -r .id)
echo "✓ Файл загружен: запись $REC_ID ($(echo "$BODY" | jq -r .sizeBytes) байт)"

TRANSCRIPTION=$(jq -n \
    --arg url "$ENGINE_URL" --arg key "$ENGINE_KEY" --arg model "$ENGINE_MODEL" --arg lang "$LANGUAGE" \
    '{engine: {baseUrl: $url, apiKey: $key, model: $model}, language: (if $lang == "" then null else $lang end)}')

request -X POST "$API_URL/recordings/$REC_ID/transcriptions" -H "$AUTH" \
    -H "Content-Type: application/json" -d "$TRANSCRIPTION"
[[ "$CODE" == "202" ]] || fail "Не удалось запустить расшифровку"
TR_ID=$(echo "$BODY" | jq -r .id)
echo "✓ Расшифровка поставлена в очередь: $TR_ID ($ENGINE_MODEL @ $ENGINE_URL)"

DEADLINE=$((SECONDS + TIMEOUT))
LAST=""
while (( SECONDS < DEADLINE )); do
    request "$API_URL/transcriptions/$TR_ID" -H "$AUTH"
    [[ "$CODE" == "200" ]] || fail "Не удалось получить статус"
    STATUS=$(echo "$BODY" | jq -r .status)
    [[ "$STATUS" != "$LAST" ]] && echo "  статус: $STATUS" && LAST="$STATUS"

    case "$STATUS" in
        done)
            echo
            echo "✓ Готово:"
            echo "$BODY" | jq -r .fullText
            if [[ -n "$DOWNLOAD" ]]; then
                mkdir -p "$DOWNLOAD"
                SAVED=$(curl -s -f -OJ --output-dir "$DOWNLOAD" -w '%{filename_effective}' \
                    "$API_URL/transcriptions/$TR_ID/export" -H "$AUTH") \
                    || { echo "✗ Не удалось скачать файл расшифровки" >&2; exit 1; }
                echo
                echo "✓ Файл сохранён: $SAVED"
            fi
            exit 0
            ;;
        failed)
            echo
            echo "✗ Ошибка расшифровки:" >&2
            echo "$BODY" | jq -r .errorMessage >&2
            exit 1
            ;;
    esac
    sleep 2
done

echo "✗ Не дождался результата за ${TIMEOUT} с (последний статус: $LAST)" >&2
exit 1
