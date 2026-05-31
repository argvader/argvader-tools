(ns ttrpg-session.db.store
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [cheshire.core :as json]
   [env.config :as env]
   [com.stuartsierra.component :as component]))

(defonce db (atom nil))

(defrecord ScribeDB []
  component/Lifecycle
  (start [this]
    (println "Connecting to ttrpg-session db")
    (reset! db (jdbc/get-datasource (:ttrpg-db env/config)))
    this)
  (stop [this]
    (println "Closing ttrpg-session db connection")
    (reset! db nil)
    this))

(defn new-connection []
  {:ttrpg-db (map->ScribeDB {})})

;; --- helpers ---

(defn- conn [] (jdbc/get-connection @db))
(defn- opts [] {:builder-fn rs/as-unqualified-lower-maps})

(defn- find-one [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute-one! c sql-vec (opts))))

(defn- query-for [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute! c sql-vec (opts))))

(defn- execute! [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute-one! c sql-vec {:return-keys true :builder-fn rs/as-unqualified-lower-maps})))

(defn parse-jsonb
  "Parse a JSONB value from PostgreSQL. Handles PGobject, String, or nil."
  [v]
  (cond
    (nil? v)    nil
    (string? v) (json/parse-string v true)
    (instance? org.postgresql.util.PGobject v)
      (json/parse-string (.getValue ^org.postgresql.util.PGobject v) true)
    :else v))

;; --- recording sessions ---

(defn create-recording-session! [{:keys [discord-guild channel-name campaign-name]}]
  (execute! ["INSERT INTO recording_sessions (discord_guild, channel_name, campaign_name)
              VALUES (?, ?, ?) RETURNING *"
             discord-guild channel-name campaign-name]))

(defn find-recording-session [session-id]
  (find-one ["SELECT * FROM recording_sessions WHERE id = ?"
             (java.util.UUID/fromString session-id)]))

(defn list-recording-sessions []
  (query-for ["SELECT * FROM recording_sessions ORDER BY started_at DESC"]))

(defn list-sessions-with-titles
  "Returns all sessions with their summary titles and 1-based session numbers."
  []
  (query-for ["SELECT rs.*,
                      ss.session_title,
                      ROW_NUMBER() OVER (ORDER BY rs.started_at) AS session_number
               FROM recording_sessions rs
               LEFT JOIN session_summaries ss ON ss.session_id = rs.id
               ORDER BY rs.started_at"]))

(defn update-session-status! [session-id status]
  (execute! ["UPDATE recording_sessions SET status = ? WHERE id = ? RETURNING *"
             status (java.util.UUID/fromString session-id)]))

(defn end-recording-session! [session-id]
  (execute! ["UPDATE recording_sessions SET status = 'processing', ended_at = now()
              WHERE id = ? RETURNING *"
             (java.util.UUID/fromString session-id)]))

(defn fail-recording-session! [session-id error-msg]
  (execute! ["UPDATE recording_sessions SET status = 'error', error_msg = ?
              WHERE id = ? RETURNING *"
             error-msg (java.util.UUID/fromString session-id)]))

(defn complete-recording-session! [session-id]
  (execute! ["UPDATE recording_sessions SET status = 'done' WHERE id = ? RETURNING *"
             (java.util.UUID/fromString session-id)]))

;; --- speaker tracks ---

(defn create-speaker-track! [{:keys [session-id discord-user-id discord-username character-name]}]
  (execute! ["INSERT INTO speaker_tracks (session_id, discord_user_id, discord_username, character_name)
              VALUES (?, ?, ?, ?) RETURNING *"
             (java.util.UUID/fromString session-id)
             discord-user-id discord-username character-name]))

(defn update-track-transcript! [track-id transcript]
  (execute! ["UPDATE speaker_tracks SET transcript = ?::jsonb WHERE id = ? RETURNING *"
             (json/generate-string transcript)
             (java.util.UUID/fromString track-id)]))

(defn find-tracks-for-session [session-id]
  (query-for ["SELECT * FROM speaker_tracks WHERE session_id = ? ORDER BY created_at"
              (java.util.UUID/fromString session-id)]))

;; --- session summaries ---

(defn create-summary!
  [{:keys [session-id transcript
           session-title overview key-events-detail
           memorable-moments lore-learned items mysteries
           commitments next-steps timeline
           scene-title scene-prose scene-image-path
           pc-moments]}]
  (execute! ["INSERT INTO session_summaries
              (session_id, transcript,
               session_title, overview, key_events_detail,
               memorable_moments, lore_learned, items, mysteries,
               commitments, next_steps, timeline_entries,
               scene_title, scene_prose, scene_image_path, pc_moments)
              VALUES (?, ?::jsonb,
                      ?, ?, ?::jsonb,
                      ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb,
                      ?::jsonb, ?::jsonb, ?::jsonb,
                      ?, ?, ?, ?::jsonb)
              RETURNING *"
             (java.util.UUID/fromString session-id)
             (json/generate-string transcript)
             session-title overview (json/generate-string key-events-detail)
             (json/generate-string memorable-moments)
             (json/generate-string lore-learned)
             (json/generate-string items)
             (json/generate-string mysteries)
             (json/generate-string commitments)
             (json/generate-string next-steps)
             (json/generate-string timeline)
             scene-title scene-prose scene-image-path
             (json/generate-string pc-moments)]))

(defn update-summary-published! [session-id page-path]
  (execute! ["UPDATE session_summaries SET site_s3_key = ?, published_at = now()
              WHERE session_id = ? RETURNING *"
             page-path (java.util.UUID/fromString session-id)]))

(defn find-summary [session-id]
  (find-one ["SELECT * FROM session_summaries WHERE session_id = ?"
             (java.util.UUID/fromString session-id)]))

(defn find-pc-moments-across-sessions
  "Returns all session summaries that have pc_moments, with session number."
  []
  (query-for ["SELECT ss.pc_moments,
                      ss.session_title,
                      rs.started_at,
                      rs.id AS session_id,
                      ROW_NUMBER() OVER (ORDER BY rs.started_at) AS session_number
               FROM session_summaries ss
               JOIN recording_sessions rs ON rs.id = ss.session_id
               WHERE ss.pc_moments IS NOT NULL
               ORDER BY rs.started_at"]))

;; --- npcs ---

(defn upsert-npc! [{:keys [slug name description]}]
  (execute! ["INSERT INTO npcs (slug, name, description)
              VALUES (?, ?, ?)
              ON CONFLICT (slug) DO UPDATE
                SET name = EXCLUDED.name,
                    description = EXCLUDED.description
              RETURNING *"
             slug name description]))

(defn add-npc-session-note! [{:keys [npc-id session-id notes]}]
  (execute! ["INSERT INTO npc_session_notes (npc_id, session_id, notes)
              VALUES (?, ?, ?)
              ON CONFLICT (npc_id, session_id) DO NOTHING"
             npc-id (java.util.UUID/fromString session-id) notes]))

(defn list-npcs []
  (query-for ["SELECT * FROM npcs ORDER BY name"]))

(defn find-notes-for-npc
  "Returns session notes for an NPC with session metadata, ordered by session date."
  [npc-id]
  (query-for ["SELECT nsn.notes, nsn.session_id, rs.started_at, ss.session_title,
                      ROW_NUMBER() OVER (ORDER BY rs.started_at) AS session_number
               FROM npc_session_notes nsn
               JOIN recording_sessions rs ON rs.id = nsn.session_id
               LEFT JOIN session_summaries ss ON ss.session_id = nsn.session_id
               WHERE nsn.npc_id = ?
               ORDER BY rs.started_at"
              npc-id]))

;; --- locations ---

(defn upsert-location! [{:keys [slug name description]}]
  (execute! ["INSERT INTO locations (slug, name, description)
              VALUES (?, ?, ?)
              ON CONFLICT (slug) DO UPDATE
                SET name = EXCLUDED.name,
                    description = EXCLUDED.description
              RETURNING *"
             slug name description]))

(defn add-location-session-note! [{:keys [location-id session-id notes]}]
  (execute! ["INSERT INTO location_session_notes (location_id, session_id, notes)
              VALUES (?, ?, ?)
              ON CONFLICT (location_id, session_id) DO NOTHING"
             location-id (java.util.UUID/fromString session-id) notes]))

(defn list-locations []
  (query-for ["SELECT * FROM locations ORDER BY name"]))

(defn find-notes-for-location
  "Returns session notes for a location with session metadata, ordered by session date."
  [location-id]
  (query-for ["SELECT lsn.notes, lsn.session_id, rs.started_at, ss.session_title,
                      ROW_NUMBER() OVER (ORDER BY rs.started_at) AS session_number
               FROM location_session_notes lsn
               JOIN recording_sessions rs ON rs.id = lsn.session_id
               LEFT JOIN session_summaries ss ON ss.session_id = lsn.session_id
               WHERE lsn.location_id = ?
               ORDER BY rs.started_at"
              location-id]))
