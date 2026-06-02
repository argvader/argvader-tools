(ns ttrpg-session.server
  (:require
   [com.stuartsierra.component :as component]
   [ttrpg-session.routes :as routes]
   [io.pedestal.http :as http]))

(defrecord Server [ttrpg-db server]
  component/Lifecycle

  (start [this]
    (println "Starting ttrpg-session server on port 8888")
    (assoc this :server
           (-> {::http/routes            (routes/routes ttrpg-db)
                ::http/type              :jetty
                ::http/host              "0.0.0.0"
                ::http/port              8888
                ::http/join?             false
                ::http/allowed-origins   {:creds true :allowed-origins (constantly true)}
                ::http/container-options {:ssl? false}}
               http/create-server
               http/start)))

  (stop [this]
    (when server
      (http/stop server))
    (assoc this :server nil)))

(defn new-server []
  {:server (component/using (map->Server {}) [:ttrpg-db])})
