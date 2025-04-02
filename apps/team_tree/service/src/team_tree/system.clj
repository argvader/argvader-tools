(ns team-tree.system
  (:require
    [com.stuartsierra.component :as component]
    [team-tree.schema :as schema]
    [team-tree.server :as server]
    [repository.team-tree-db :as team-tree-db]
    [repository.code-churn-db :as code-churn-db]))

(defn new-system
  []
  (merge (component/system-map)
         (team-tree-db/new-connection)
         (code-churn-db/new-connection)
         (server/new-server)
         (schema/new-schema-provider)))
