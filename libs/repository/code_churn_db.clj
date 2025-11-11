(ns repository.code-churn-db
  (:require [next.jdbc  :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.transaction :as tx]
            [env.config   :as env]
            [com.stuartsierra.component :as component]))

(defonce db (atom nil))

(defn- load-config []
      (:code-churn-db env/config))

(defrecord CodeChurnDB []
  component/Lifecycle
  (start [this]
    (println "Connection to code churn db [connecting]")
    (reset! db (jdbc/get-datasource (load-config))))
  (stop [this]
    (println "Connection to code churn db [closing]")
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
  {:code-churn-db (map->CodeChurnDB {})})
