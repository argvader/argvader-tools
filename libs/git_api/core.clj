(ns git-api.core
  (:require [com.stuartsierra.component :as component]
            [git-api.graphql :as graph]
            [git-api.cli :as cli]
            [git-api.rest :as rest]
            [env.config   :as env]))


(defrecord Git []
  component/Lifecycle

  (start [this]
    (println "Starting git:")
    (graph/init (get-in env/config [:secrets :git-graphql-token]))
    (cli/init (:git-cli env/config))
    (rest/init (get-in env/config [:secrets :git-graphql-token])))
  (stop [this]
    (println "Stopping git:")
    (graph/close)
    (cli/close)
    (rest/close)))

(defn new-connection []
  {:git (map->Git {})})

(def query graph/query)
(def query-sync graph/query-sync)
(def find-contributors rest/find-contributors)
