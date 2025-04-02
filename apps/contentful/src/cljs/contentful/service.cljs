(ns contentful.service
  (:require [cljs.core.async :refer [go <!]]
            [clojure.string :as str]
            [contentful.utils.slack-formatter :refer [format-text format-error]]
            [contentful.features.teams :as teams]
            [contentful.features.spaces :as spaces]
            [contentful.features.users :as users]
            [contentful.features.check-ad-group :refer [ad-member? reminder-group-message]]))

(def contentful-admin-channel "C03KTPHQ1J7")

(defn- help-message []
  (go
    (concat
      (spaces/help)
      [{:type "divider"}]
      (teams/help))))

(defn- admin-channel [command handler]
  (if (= (:channel command) contentful-admin-channel)
    (handler (:who command) (:text command))
    (go (format-text ["I'll only do that in #contentful-admin channel."]))))

(defn- build-blocks [persist data]
  (assoc {} :blocks data :response_type (if persist "in_channel" "ephemeral")))

(defn- transient-response [data]
  (build-blocks false data))

(defn- permanent-response [data]
  (build-blocks true data))

(defn- route-command [command]
  (go
    (let [cmd (:command command)
          admin-channel? (partial admin-channel command)]
       (try
         (cond
           (= cmd "list-teams") (transient-response (<! (teams/list-teams)))
           (= cmd "list-spaces") (transient-response (<! (spaces/list-spaces)))
           (= cmd "my-spaces") (transient-response (<! (spaces/my-spaces (:who command))))
           (= cmd "my-teams") (transient-response (<! (teams/my-teams (:who command))))
           (= cmd "add-space") (permanent-response (<! (admin-channel? spaces/add-space)))
           (= cmd "add-team")  (permanent-response (<! (admin-channel? teams/add-team)))
           (= cmd "remove-space") (permanent-response (<! (admin-channel? spaces/remove-space)))
           (= cmd "remove-team") (permanent-response (<! (admin-channel? teams/remove-team)))
           (= cmd "help") (transient-response (<! (help-message)))
           :else (go (transient-response (format-text ["command invalid"]))))
         (catch js/Error e
           (let [parsed-error (js->clj e)]
             (go (transient-response (format-error ["Oops! Error occured:" (str (:name parsed-error) ":" (:message parsed-error))])))))
         (catch :default e
           (let [parsed-error (js->clj e)]
             (go (transient-response (format-error ["Eeek! Bad things happened."])))))))))

(defn process-command [command]
   (go
      (<! (route-command command))))
