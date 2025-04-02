(ns contentful.features.spaces
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [contentful.gateways.contentful-api :as contentful-api]
            [contentful.utils.slack-formatter :refer [format-text]]
            [contentful.features.users :refer [handle-user-registration]]
            [contentful.features.check-ad-group :refer [reminder-group-message]]
            [clojure.string :as str]))

(defn- include-spaces [all-spaces user-spaces key]
  (let [str-keys (str/join "," user-spaces)]
    (filter #(str/includes? str-keys (get % key)) all-spaces)))

(defn- user-spaces [email]
  (go
     (-> email
         (contentful-api/find-sys-id)
         (<!)
         (contentful-api/space-memberships)
         (<!))))

(defn- list-response [data]
  (concat
      [{:type "section" :text {:type "mrkdwn" :text "*Spaces*"}}]
      data))

(defn- mutation-response [response success failed]
  (if (empty? response)
     (format-text [failed])
     (format-text [success])))

(defn list-spaces []
  (go
    (let [spaces (<! (contentful-api/list-spaces))]
      (->> spaces
           (map #(str "* " (get % :name)))
           (str/join "\n")
           (vector)
           (format-text)
           (list-response)))))

(defn my-spaces [email]
  (go
    (let [all-spaces (<! (contentful-api/list-spaces))
          user-spaces (<! (user-spaces email))]
      (->> (include-spaces all-spaces user-spaces :id)
           (map #(str "* " (get % :name)))
           (str/join "\n")
           (vector)
           (format-text)
           (list-response)))))

(defn find-space [name]
  (go
     (let [all-spaces (<! (contentful-api/list-spaces))]
       (first (concat
                 (include-spaces all-spaces [name] :id)
                 (include-spaces all-spaces [name] :name))))))

(defn add-space [email space-name]
  (go
    (<! (handle-user-registration email))
    (let [space (<! (find-space space-name))
          response (<! (contentful-api/add-space (:id space) email))]
      (concat
        (mutation-response response (str "Added " email " to " space-name ".") (str "Failed to add " email " to " (:name space)))
        (reminder-group-message email)))))

(defn remove-space [email space-name]
  (go
    (let [space (<! (find-space space-name))
          user-id (<! (contentful-api/find-sys-id email))
          membership-id (<! (contentful-api/user-space-membership user-id (:id space)))
          response (<! (contentful-api/remove-space (:id space) membership-id))]
      (mutation-response response (str "Removed " email " from " space-name ".") (str "Failed to remove " email " from " (:name space))))))

(defn help []
   (format-text
     ["*add-space <user> [name]* Adds named space to slack user provided or slack user executing command. (ie /contentful-mgr add-space @gary.paige Glow)"
      "*remove-space <user> [name]* removes named space from slack user provided or slack user executing command. (ie /contentful-mgr remove-space @gary.paige Glow)"
      "*list-spaces* Lists all the spaces in organization (ie /contentful-mgr list-spaces)"
      "*my-spaces* Lists all users spaces (ie /contentful-mgr my-spaces)"]))
