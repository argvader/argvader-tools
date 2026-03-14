(ns ttrpg-session.migrate
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [next.jdbc :as jdbc]))

(defn- ensure-migrations-table! [conn]
  (jdbc/execute! conn
    ["CREATE TABLE IF NOT EXISTS schema_migrations (
        id          BIGINT PRIMARY KEY,
        description TEXT NOT NULL,
        applied_at  TIMESTAMPTZ NOT NULL DEFAULT now()
      )"]))

(defn- applied-ids [conn]
  (->> (jdbc/execute! conn ["SELECT id FROM schema_migrations ORDER BY id"])
       (map :schema_migrations/id)
       set))

(defn- migration-files []
  (->> (file-seq (io/file "resources/migrations"))
       (filter #(str/ends-with? (.getName %) ".up.sql"))
       (sort-by #(.getName %))))

(defn- parse-id-and-desc [filename]
  (let [[_ id desc] (re-find #"^(\d+)-(.+)\.up\.sql$" filename)]
    [(Long/parseLong id) (str/replace desc #"-" " ")]))

(defn- run-migration! [conn file]
  (let [filename  (.getName file)
        [id desc] (parse-id-and-desc filename)
        sql       (slurp file)
        stmts     (->> (str/split sql #"--\s*;;\s*")
                       (map str/trim)
                       (remove str/blank?))]
    (println (str "Applying migration " id ": " desc))
    (doseq [stmt stmts]
      (jdbc/execute! conn [stmt]))
    (jdbc/execute! conn
      ["INSERT INTO schema_migrations (id, description) VALUES (?, ?)" id desc])
    (println (str "  Applied " (count stmts) " statement(s)"))))

(defn -main [& [profile]]
  (let [kw-profile (keyword (or profile "dev"))
        config     (aero/read-config (io/resource "config.edn") {:profile kw-profile})
        db-spec    (:ttrpg-db config)
        ds         (jdbc/get-datasource db-spec)]
    (with-open [conn (jdbc/get-connection ds)]
      (ensure-migrations-table! conn)
      (let [done    (applied-ids conn)
            pending (->> (migration-files)
                         (filter #(let [[id _] (parse-id-and-desc (.getName %))]
                                    (not (done id)))))]
        (if (empty? pending)
          (println "No pending migrations.")
          (doseq [f pending]
            (run-migration! conn f)))))))
