(ns translation.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]))

(defn- healthcheck [request]
  {:status 200
   :body "translation-api OK"})

(defroutes routes
  #{["/health" :get healthcheck :route-name :health]})
