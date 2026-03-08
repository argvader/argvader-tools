(ns fable.db.store
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [cheshire.core :as json]
   [env.config :as env]
   [com.stuartsierra.component :as component]))

(defonce db (atom nil))

(defn- load-config []
  (:fable-db env/config))

(defrecord FableDB []
  component/Lifecycle
  (start [this]
    (println "Connecting to fable db")
    (reset! db (jdbc/get-datasource (load-config)))
    this)
  (stop [this]
    (println "Closing fable db connection")
    (reset! db nil)
    this))

(defn new-connection []
  {:fable-db (map->FableDB {})})

;; -- helpers --

(defn- conn [] (jdbc/get-connection @db))

(defn- opts [] {:builder-fn rs/as-unqualified-lower-maps})

(defn find-one [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute-one! c sql-vec (opts))))

(defn query-for [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute! c sql-vec (opts))))

(defn execute! [sql-vec]
  (with-open [c (conn)]
    (jdbc/execute-one! c sql-vec {:return-keys true :builder-fn rs/as-unqualified-lower-maps})))

;; -- sessions --

(defn create-session! [{:keys [meta-token user-name user-id expires-at]}]
  (execute! [(str "INSERT INTO sessions (meta_token, user_name, user_id, expires_at)"
                  " VALUES (?, ?, ?, ?) RETURNING *")
             meta-token user-name user-id expires-at]))

(defn find-session [session-id]
  (find-one ["SELECT * FROM sessions WHERE id = ? AND (expires_at IS NULL OR expires_at > now())"
             (java.util.UUID/fromString session-id)]))

(defn delete-session! [session-id]
  (execute! ["DELETE FROM sessions WHERE id = ?"
             (java.util.UUID/fromString session-id)]))

;; -- storybooks --

(defn create-storybook! [{:keys [session-id theme data]}]
  (execute! ["INSERT INTO storybooks (session_id, theme, data) VALUES (?, ?, ?::jsonb) RETURNING *"
             (java.util.UUID/fromString session-id) (name theme)
             (json/generate-string data)]))

(defn find-storybook [storybook-id]
  (find-one ["SELECT * FROM storybooks WHERE id = ?"
             (java.util.UUID/fromString storybook-id)]))

(defn update-storybook-pdf! [storybook-id s3-key]
  (execute! ["UPDATE storybooks SET pdf_s3_key = ? WHERE id = ?"
             s3-key (java.util.UUID/fromString storybook-id)]))

;; -- generation jobs --

(defn create-job! [storybook-id]
  (execute! ["INSERT INTO generation_jobs (storybook_id) VALUES (?) RETURNING *"
             (java.util.UUID/fromString storybook-id)]))

(defn find-job [job-id]
  (find-one ["SELECT * FROM generation_jobs WHERE id = ?"
             (java.util.UUID/fromString job-id)]))

(defn update-job! [job-id {:keys [status progress error-msg]}]
  (execute! [(str "UPDATE generation_jobs"
                  " SET status = ?, progress = ?, error_msg = ?, updated_at = now()"
                  " WHERE id = ? RETURNING *")
             status progress error-msg
             (java.util.UUID/fromString job-id)]))
