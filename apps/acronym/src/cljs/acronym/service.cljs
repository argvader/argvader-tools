(ns acronym.service
  (:require [cljs.core.async :refer [go <!]]
            [clojure.string :as str]
            ["@2toad/profanity" :as profanity-filter]
            [acronym.gateway :refer [get-define, put-define!, list-definitions]]))

(def define "/define")
(def profanity (.-profanity profanity-filter))

(defn format-response [data]
   (map #(conj {} {:type "section" :text {:type "mrkdwn" :text %}}) data))

(defn handle-new-definition [acronym definition]
  (go
    (let [data (<! (put-define! (str/upper-case acronym) definition))]
      (format-response [(str "Your definition for " acronym " has been added.")]))))

(defn handle-find-definition [acronym]
  (go
    (let [upper (str/upper-case acronym)
          data (<! (get-define upper))
          results [(str "*" upper " definitions:*")]]
      (if (nil? data)
         (format-response (concat results ["No definitions found."]))
         (format-response (concat results (vec (map #(.censor profanity (str %)) (.-definition data)))))))))

(defn process-command [command]
  (let [cmd (:command command)
        values (str/split (:text command) #" ")]
   (go
     (cond
       (and (> (count values) 1) (= cmd define)) (<! (handle-new-definition (first values) (->> values (rest) (str/join " "))))
       (and (some? (first values)) (= cmd define)) (<! (handle-find-definition (first values)))
       :else [(format-response "command invalid")]))))
