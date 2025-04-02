(ns contentful.gateways.slack-api
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [adzerk.env :as env])
  (:require [contentful.utils.axios :refer [axios-get]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as str]))

(env/def SLACK_TOKEN :required)
(def api-url "https://slack.com/api/")
(def headers (clj->js {:Authorization (str "Bearer " SLACK_TOKEN) :Content-Type "application/json"}))

(defn- get-email [slack-id]
  (go
    (let [request-url (str api-url "users.profile.get" "?user=" slack-id)
          response (<p! (axios-get request-url headers))
          keyed-response (js->clj response :keywordize-keys true)]
      (get-in keyed-response [:data :profile :email]))))

(defn- matcher [regex value]
  (if-some [[whole-match group]
            (re-find regex (if (nil? value) "" value))]
   [true whole-match group]
   [false]))

(defn- match-slack [value]
  (let [[match whole slack-user] (matcher #"^<@(.*)\|(.*)>" value)]
    (if match
      [true slack-user :slack]
      [false ""])))

(defn- match-email [value]
  (let [[match whole email] (matcher #"^(.*)@asurion.com" value)]
    (if match
      [true whole :email]
      [false ""])))

(defn- determine-who [value user-id]
  (first (filter #(first %) [(match-email value) (match-slack value) [true user-id :user-id]])))

(defn- determine-text [second user-id the-rest]
  (let [[match value type] (determine-who second user-id)]
    (if (= type :user-id)
     (flatten (vector second the-rest))
     the-rest)))

(defn- strip-formatting [text]
  (let [regex #"^<(mailto:|https?://)(.*)\|(.*)>"]
    (if (re-find regex (if (nil? text) "" text))
      (str/replace text regex "$2")
      text)))

(defn- find-text [value rest user-id]
  {:text (str/join " " (determine-text value user-id rest))})

(defn- find-who [value user-id]
  (go
    (let [[match name type] (determine-who value user-id)]
      (if (= type :email)
        {:who name}
        {:who (<! (get-email name))}))))

(defn build-command [request]
  (go
    (let [[command second & the-rest] (map strip-formatting (str/split (:text request) #" "))
          {user_id :user_id
           channel_id :channel_id} request]
      (conj {:channel channel_id}
            {:command command}
            (find-text second the-rest user_id)
            (<! (find-who second user_id))))))
