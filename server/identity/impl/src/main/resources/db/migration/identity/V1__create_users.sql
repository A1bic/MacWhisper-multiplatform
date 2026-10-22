CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT(gen_random_uuid()),
    email TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    display_name TEXT CHECK(length(display_name) <= 100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT(now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT(now())
);