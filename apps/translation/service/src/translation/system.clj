(ns translation.system
  (:require
    [com.stuartsierra.component :as component]
    [translation.server :as server]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)))
