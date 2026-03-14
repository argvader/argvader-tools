CREATE TABLE recording_sessions (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  discord_guild TEXT NOT NULL,
  channel_name  TEXT NOT NULL,
  campaign_name TEXT,
  status        TEXT NOT NULL DEFAULT 'recording',
  started_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  ended_at      TIMESTAMPTZ,
  error_msg     TEXT
)

-- ;;

CREATE TABLE speaker_tracks (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id       UUID REFERENCES recording_sessions(id) ON DELETE CASCADE,
  discord_user_id  TEXT NOT NULL,
  discord_username TEXT NOT NULL,
  character_name   TEXT,
  transcript       JSONB,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
)

-- ;;

CREATE TABLE session_summaries (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id      UUID REFERENCES recording_sessions(id) ON DELETE CASCADE UNIQUE,
  transcript      JSONB NOT NULL,
  narrative       TEXT NOT NULL,
  key_events      JSONB NOT NULL,
  notable_quotes  JSONB NOT NULL,
  site_s3_key     TEXT,
  published_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
)
