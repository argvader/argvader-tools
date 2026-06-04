-- Drop legacy session_summaries columns superseded by 002-wiki.
-- The pipeline now writes overview / key_events_detail / memorable_moments instead;
-- these NOT NULL leftovers from 001-init were blocking every summary insert.
ALTER TABLE session_summaries
  DROP COLUMN IF EXISTS narrative,
  DROP COLUMN IF EXISTS key_events,
  DROP COLUMN IF EXISTS notable_quotes
