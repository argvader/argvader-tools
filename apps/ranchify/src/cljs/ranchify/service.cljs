(ns ranchify.service
  (:require [cljs.core.async :refer [go <!]]
            [ranchify.gpt :refer [translate-cowboy]]
            [clojure.string :as str]))

(def ranchify "/ranchify")

(defn format-response [data]
  (map #(conj {} {:type "section" :text {:type "mrkdwn" :text %}}) data))

(defn handle-ranchification [user-text]
  (print user-text)
  (go
    (let [data (<! (translate-cowboy user-text))]
      (format-response [data]))))

(defn process-command [command]
  (let [cmd (:command command)
        values (:text command)]
   (print cmd)
   (print values)
   (go
     (cond
       (and (not-empty values) (= cmd ranchify)) (<! (handle-ranchification values))
       :else [(format-response "command invalid")]))))
