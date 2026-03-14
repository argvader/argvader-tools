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

;; -- helpers --

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

;; -- recording sessions --

(defn create-recording-session! [{:keys [discord-guild channel-name campaign-name]}]
  (execute! ["INSERT INTO recording_sessions (discord_guild, channel_name, campaign_name)
              VALUES (?, ?, ?) RETURNING *"
             discord-guild channel-name campaign-name]))

(defn find-recording-session [session-id]
  (find-one ["SELECT * FROM recording_sessions WHERE id = ?"
             (java.util.UUID/fromString session-id)]))

(defn list-recording-sessions []
  (query-for ["SELECT * FROM recording_sessions ORDER BY started_at DESC"]))

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

;; -- speaker tracks --

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

;; -- session summaries --

(defn create-summary! [{:keys [session-id transcript narrative key-events notable-quotes]}]
  (execute! ["INSERT INTO session_summaries
                (session_id, transcript, narrative, key_events, notable_quotes)
              VALUES (?, ?::jsonb, ?, ?::jsonb, ?::jsonb) RETURNING *"
             (java.util.UUID/fromString session-id)
             (json/generate-string transcript)
             narrative
             (json/generate-string key-events)
             (json/generate-string notable-quotes)]))

(defn update-summary-published! [session-id s3-key]
  (execute! ["UPDATE session_summaries SET site_s3_key = ?, published_at = now()
              WHERE session_id = ? RETURNING *"
             s3-key (java.util.UUID/fromString session-id)]))

(defn find-summary [session-id]
  (find-one ["SELECT * FROM session_summaries WHERE session_id = ?"
             (java.util.UUID/fromString session-id)]))
