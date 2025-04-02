(ns contentful.features.teams
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [contentful.gateways.contentful-api :as contentful-api]
            [cljs.core.async.interop :refer-macros [<p!]]
            [contentful.features.check-ad-group :refer [reminder-group-message]]
            [contentful.features.users :refer [handle-user-registration]]
            [contentful.utils.slack-formatter :refer [format-text]]
            [clojure.string :as str]))

(defn- include-teams [all-teams user-teams key]
  (let [str-keys (str/join "," user-teams)]
    (filter #(str/includes? str-keys (get % key)) all-teams)))

(defn- list-response [data]
  (concat
      [{:type "section" :text {:type "mrkdwn" :text "*Teams*"}}]
      data))

(defn- mutation-response [response success failed]
  (if (empty? response)
     (format-text [failed])
     (format-text [success])))

(defn- user-teams [email]
  (go
     (-> email
         (contentful-api/find-user-org-id)
         (<!)
         (contentful-api/team-memberships)
         (<!))))

(defn list-teams []
  (go
    (let [teams (<! (contentful-api/list-teams))]
      (->> teams
           (map #(str "* " (get % :name)))
           (str/join "\n")
           (vector)
           (format-text)
           (list-response)))))

(defn my-teams [email]
  (go
    (let [all-teams (<! (contentful-api/list-teams))
          user-teams (<! (user-teams email))]
      (->> (include-teams all-teams user-teams :id)
           (map #(str "* " (get % :name)))
           (str/join "\n")
           (vector)
           (format-text)
           (list-response)))))


(defn find-team [name]
  (go
     (let [all-teams (<! (contentful-api/list-teams))]
       (first (include-teams all-teams [name] :name)))))

(defn add-team [email team-name]
  (go
    (<! (handle-user-registration email))
    (let [team (<! (find-team team-name))
          user-org-id (<! (contentful-api/find-user-org-id email))
          response (<! (contentful-api/add-team (:id team) user-org-id))]
      (concat
        (mutation-response response (str "Added " email " to " team-name ".") (str "Failed to add " email " to " team-name))
        (reminder-group-message email)))))

(defn remove-team [email team-name]
  (go
    (let [team (<! (find-team team-name))
          user-id (<! (contentful-api/find-user-org-id email))
          membership (<! (contentful-api/user-team-membership user-id (:id team)))
          response (<! (contentful-api/remove-team (:team-id membership) (:membership-id membership)))]
     (mutation-response response (str "Removed " email " from " team-name ".") (str "Failed to remove " email " from " team-name)))))

(defn help []
   (format-text
     ["*add-team <user> [name]* Adds named team to slack user provided or user executing command. (ie /contentful-mgr add-team @gary.paige Glow)"
      "*remove-team <user> [name]* removes named team from user provided. (ie /contentful-mgr remove-team @gary.paige Glow)"
      "*list-teams* Lists all the teams in organization. (ie /contentful-mgr list-teams)"
      "*my-teams* Lists all users teams. (ie /contentful-mgr my-teams"]))
