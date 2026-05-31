ALTER TABLE session_summaries
  ADD COLUMN session_title      TEXT,
  ADD COLUMN overview           TEXT,
  ADD COLUMN key_events_detail  JSONB,
  ADD COLUMN memorable_moments  JSONB,
  ADD COLUMN lore_learned       JSONB,
  ADD COLUMN items              JSONB,
  ADD COLUMN mysteries          JSONB,
  ADD COLUMN commitments        JSONB,
  ADD COLUMN next_steps         JSONB,
  ADD COLUMN timeline_entries   JSONB,
  ADD COLUMN scene_title        TEXT,
  ADD COLUMN scene_prose        TEXT,
  ADD COLUMN scene_image_path   TEXT,
  ADD COLUMN pc_moments         JSONB

-- ;;

CREATE TABLE npcs (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug        TEXT UNIQUE NOT NULL,
  name        TEXT NOT NULL,
  description TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
)

-- ;;

CREATE TABLE npc_session_notes (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  npc_id     UUID NOT NULL REFERENCES npcs(id) ON DELETE CASCADE,
  session_id UUID NOT NULL REFERENCES recording_sessions(id) ON DELETE CASCADE,
  notes      TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (npc_id, session_id)
)

-- ;;

CREATE TABLE locations (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  slug        TEXT UNIQUE NOT NULL,
  name        TEXT NOT NULL,
  description TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
)

-- ;;

CREATE TABLE location_session_notes (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  location_id UUID NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
  session_id  UUID NOT NULL REFERENCES recording_sessions(id) ON DELETE CASCADE,
  notes       TEXT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (location_id, session_id)
)
