(ns thank-ewe.handler
  (:require [cljs.core.async :refer [go <!]]
            [thank-ewe.utils.form :as form]
            [thank-ewe.utils.authorizer :as authorizer]
            [thank-ewe.service :as service]))

(defn- build-command [event]
  (-> event (.-body) (form/decode)))

(defn- build-response [payload]
  (clj->js {:statusCode 200
            :body (.stringify js/JSON (clj->js payload))
            :headers {"Content-Type" "application/json"}}))

(defn handler [event _ cb]
  (go
    (if (authorizer/authorize event)
      (cb nil
          (-> event
              (build-command)
              (service/process-command)
              (<!)
              (build-response)))
      (cb nil (clj->js {:statusCode 403 :body ""})))))

