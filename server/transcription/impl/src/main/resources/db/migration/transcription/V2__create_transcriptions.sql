CREATE TABLE transcriptions(
    id UUID PRIMARY KEY DEFAULT(gen_random_uuid()),
    recording_id UUID NOT NULL REFERENCES recordings.recordings(id) ON DELETE CASCADE,
    status job_status NOT NULL DEFAULT('queued'),
    model TEXT NOT NULL,
    language TEXT,
    full_text TEXT,
    result_key TEXT,
    error_message TEXT,
    attempts INTEGER NOT NULL DEFAULT(0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT(now()),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    search_tsv TSVECTOR GENERATED ALWAYS AS ( to_tsvector('russian', coalesce(full_text, '')) ) STORED
);