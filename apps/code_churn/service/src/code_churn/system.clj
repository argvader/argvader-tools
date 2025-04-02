(ns code-churn.system
  (:require
    [com.stuartsierra.component :as component]
    [code-churn.server :as server]
    [git-api.core :as git]
    [repository.code-churn-db :as code-churn-db]
    [code-churn.tester :as tester]))

(defn new-system
  []
  (merge (component/system-map)
         (code-churn-db/new-connection)
         (git/new-connection)
         (server/new-server)))
         ;(tester/new-test)))
