CREATE TABLE recordings (
    id UUID PRIMARY KEY DEFAULT(gen_random_uuid()),
    user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    original_filename TEXT,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL,
    duration_ms BIGINT,
    storage_bucket TEXT NOT NULL,
    storage_key TEXT NOT NULL,
    checksum_sha256 TEXT CHECK(length(checksum_sha256) = 64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT(now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT(now()),
    deleted_at TIMESTAMPTZ
)