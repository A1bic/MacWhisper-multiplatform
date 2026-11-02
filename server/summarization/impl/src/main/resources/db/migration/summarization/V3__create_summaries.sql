CREATE TABLE summaries(
    id UUID PRIMARY KEY DEFAULT(gen_random_uuid()),
    transcription_id UUID NOT NULL REFERENCES transcription.transcriptions(id) ON DELETE CASCADE,
    kind summary_kind NOT NULL DEFAULT('short'),
    status job_status NOT NULL DEFAULT('queued'),
    model TEXT NOT NULL,
    prompt TEXT,
    content TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT(now()),
    finished_at TIMESTAMPTZ
)