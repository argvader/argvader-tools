(ns team-tree.server
  (:require [env.config :as env]
            [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [ring.middleware.cors :as cors]
            [io.pedestal.http :as http]))

(defn- healthcheck [request]
  {:status 200
   :body "team-tree-api OK"})

(defn addhealthcheck [service-map]
  (let [routes (::http/routes service-map)]
    (assoc service-map ::http/routes (conj routes ["/health" :get healthcheck :route-name :health]))))

(defrecord Server [schema-provider server]
  component/Lifecycle

  (start [this]
    (assoc this :server (-> schema-provider
                            :schema
                            (lp/service-map {
                                              :graphiql (get-in env/config [:graphql :iql])
                                              :subscriptions true})
                            (addhealthcheck)
                            (assoc ::http/host "0.0.0.0"
                                   ::http/type :jetty
                                   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
                                   ::http/container-options {:ssl? false})
                            http/create-server
                            http/start)))

  (stop [this]
     (http/stop server)
     (assoc this :server nil)))

(defn new-server
  []
  {:server (component/using (map->Server {})
                            [:schema-provider :team-tree-db])})
