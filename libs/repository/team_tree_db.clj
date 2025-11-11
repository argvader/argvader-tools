(ns repository.team-tree-db
  (:require [next.jdbc  :as jdbc]
            [next.jdbc.result-set :as rs]
            [env.config   :as env]
            [com.stuartsierra.component :as component]))

(defonce db (atom nil))

(defn- load-config []
      (:team-tree-db env/config))

(defrecord TeamTreeDB []
  component/Lifecycle
  (start [this]
    (println "Connection to team tree db [connecting]")
    (reset! db (jdbc/get-datasource (load-config))))
  (stop [this]
    (println "Connection to team tree db [closing]")
    (.close @db)
    (reset! db nil)))

(defn get-connection[]
  (jdbc/get-connection @db))

(defn find-one [sql]
  (with-open [conn (get-connection)]
    (jdbc/execute-one!
      conn
      sql
      {:return-keys true :builder-fn rs/as-unqualified-lower-maps})))

(defn query-for [sql]
  (with-open [conn (get-connection)]
    (jdbc/execute!
      conn
      sql
      {:return-keys true :builder-fn rs/as-unqualified-lower-maps})))

(defn new-connection []
  {:team-tree-db (map->TeamTreeDB {})})
