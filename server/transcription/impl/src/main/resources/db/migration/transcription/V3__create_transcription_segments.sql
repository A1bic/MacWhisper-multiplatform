CREATE TABLE transcript_segments (
    PRIMARY KEY(transcription_id, seq),
    transcription_id UUID NOT NULL REFERENCES transcriptions(id) ON DELETE CASCADE,
    seq INTEGER NOT NULL,
    start_ms BIGINT NOT NULL,
    end_ms BIGINT NOT NULL,
    text TEXT NOT NULL,
    speaker TEXT,
    confidence REAL
)