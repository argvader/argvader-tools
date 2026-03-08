CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE sessions (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  meta_token TEXT NOT NULL,
  user_name  TEXT,
  user_id    TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ
);

CREATE TABLE storybooks (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
  theme      TEXT NOT NULL,
  data       JSONB NOT NULL,
  pdf_s3_key TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE generation_jobs (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  storybook_id UUID REFERENCES storybooks(id) ON DELETE CASCADE,
  status       TEXT NOT NULL DEFAULT 'pending',
  progress     INT  NOT NULL DEFAULT 0,
  error_msg    TEXT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_sessions_user_id      ON sessions(user_id);
CREATE INDEX idx_storybooks_session_id ON storybooks(session_id);
CREATE INDEX idx_jobs_storybook_id     ON generation_jobs(storybook_id);
CREATE INDEX idx_jobs_status           ON generation_jobs(status);
