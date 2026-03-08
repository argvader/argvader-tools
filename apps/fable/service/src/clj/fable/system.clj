(ns fable.system
  (:require
   [com.stuartsierra.component :as component]
   [fable.db.store :as db]
   [fable.server :as server]))

(defn new-system []
  (merge
   (component/system-map)
   (db/new-connection)
   (server/new-server)))
