(ns contentful.handler
  (:require [cljs.core.async :refer [go <!]]
            [contentful.gateways.slack-api :as slack-api]
            [contentful.utils.form :as form]
            [contentful.utils.authorizer :as authorizer]
            [contentful.service :as service]))

(defn parse-request [slack-request]
  (-> slack-request
      (.-body)
      (form/decode)))

(defn build-response [data]
  (let [formatted-data (clj->js data)]
    (clj->js {:statusCode 200
              :body       (.stringify js/JSON formatted-data)
              :headers    {"Content-Type" "application/json"}})))

(defn handler [event context cb]
  (go
    (if (authorizer/authorize event)
      (cb nil
        (-> event
            (parse-request)
            (slack-api/build-command)
            (<!)
            (service/process-command)
            (<!)
            (build-response)))
      (cb nil
        (clj->js {:statusCode 403
                  :body ""})))))
