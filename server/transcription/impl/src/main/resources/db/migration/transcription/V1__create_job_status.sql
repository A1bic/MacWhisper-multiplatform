CREATE TYPE job_status AS ENUM (
    'queued',
    'processing',
	'done',
	'failed'
)