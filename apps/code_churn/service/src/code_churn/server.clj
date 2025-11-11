(ns code-churn.server
  (:require [com.stuartsierra.component :as component]
            [code-churn.routes :as routes]
            [io.pedestal.http :as http]))

(defrecord Server [schema-provider server]
  component/Lifecycle

  (start [this]
    (assoc this :server (-> {}
                            (assoc :env          "prod"
                                   ::http/routes routes/routes
                                   ::http/type   :jetty
                                   ::http/host   "0.0.0.0"
                                   ::http/port   8888
                                   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
                                   ::http/container-options {:ssl? false})
                            http/create-server
                            http/start)))

  (stop [this]
     (http/stop server)
     (assoc this :server nil)))

(defn new-server
  []
  {:server (map->Server {})})
