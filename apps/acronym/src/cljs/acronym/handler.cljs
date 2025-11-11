(ns acronym.handler
  (:require [cljs.core.async :refer [go <!]]
            [acronym.utils.form :as form]
            [acronym.utils.authorizer :as authorizer]
            [acronym.service :as service]))

(defn build-command [slack-request]
  (-> slack-request (.-body) (form/decode)))

(defn build-response [data]
  (let [formatted-data (clj->js {:blocks data})]
    (clj->js {:statusCode 200
              :body       (.stringify js/JSON formatted-data)
              :headers    {"Content-Type" "application/json"}})))

(defn handler [event context cb]
  (go
    (if (authorizer/authorize event)
      (cb nil
        (-> event
            (build-command)
            (service/process-command)
            (<!)
            (build-response)))
      (cb nil
        (clj->js {:statusCode 403
                  :body ""})))))
