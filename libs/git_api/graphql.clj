(ns git-api.graphql
  (:require [hato.client :as hc]
            [re-graph.core :as re-graph]))

(def timeout 10000)
(def http-client (hc/build-http-client {:cookie-policy :all}))

(defn init [authorizer]
  (println "graphql [connecting]")
  (re-graph/init {:http {:url "https://api.github.com/graphql"
                         :impl {:http-client http-client
                                :headers {"Authorization" (str "bearer " authorizer)}}}
                  :ws nil}))

(defn close []
  (println "graphql [closing]")
  (re-graph/destroy))

(defn- handle-response [callback, {:keys [data errors] :as payload}]
  (callback data errors))

(defn query [callback query-string arguments]
  (let [handler (partial handle-response callback)]
    (re-graph/query query-string
                    arguments
                    handler
                    timeout)))

(defn query-sync [query-string arguments]
  (re-graph/query-sync query-string
                  arguments
                  timeout))
