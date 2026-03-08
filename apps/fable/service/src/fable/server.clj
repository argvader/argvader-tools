(ns fable.server
  (:require
   [com.stuartsierra.component :as component]
   [fable.routes :as routes]
   [io.pedestal.http :as http]))

(defrecord Server [fable-db server]
  component/Lifecycle

  (start [this]
    (println "Starting fable server on port 8888")
    (assoc this :server
           (-> {::http/routes                (routes/routes fable-db)
                ::http/type                  :jetty
                ::http/host                  "0.0.0.0"
                ::http/port                  8888
                ::http/allowed-origins       {:creds true :allowed-origins (constantly true)}
                ::http/container-options     {:ssl? false}}
               http/create-server
               http/start)))

  (stop [this]
    (when server
      (http/stop server))
    (assoc this :server nil)))

(defn new-server []
  {:server (component/using (map->Server {}) [:fable-db])})
