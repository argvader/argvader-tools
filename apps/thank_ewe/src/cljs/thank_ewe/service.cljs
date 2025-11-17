(ns thank-ewe.service
  (:require [cljs.core.async :refer [go]]
            [clojure.string :as str]
            [thank-ewe.data :as data]
            [thank-ewe.utils.slack :as slack]))

(def slash-command "/thank-ewe")

(defn- slack-user [command]
  (if-let [user-id (:user_id command)]
    (str "<@" user-id ">")
    (:user_name command)))

(def mention-pattern #"^<@([^>|]+)(?:\|[^>]+)?>$")

(defn- normalize-mention [raw]
  (cond
    (nil? raw) nil
    (re-find mention-pattern raw)
    (let [[_ user-id] (re-find mention-pattern raw)]
      (str "<@" user-id ">"))
    (str/starts-with? raw "@") (str "<@" (subs raw 1) ">")
    :else raw))

(defn- parse-list [tokens]
  (if (= (second tokens) "date")
    {:action :list-by-date
     :value (nth tokens 2 nil)}
    {:action :invalid
     :message "Usage: `/thank-ewe list date [YYYY-MM-DD|now]`"}))

(defn- parse-thanks [tokens]
  (let [[mention & reason] tokens]
    (cond
      (nil? mention)
      {:action :invalid
       :message "Specify who to thank: `/thank-ewe @teammate for being awesome`"}

      (empty? reason)
      {:action :invalid
       :message "Give a reason so the leaderboard has context."}

      :else
      {:action :thank
       :mention (normalize-mention mention)
       :reason (str/join " " reason)})))

(defn- parse-command [text]
  (let [clean-text (str/trim (or text ""))
        tokens (remove str/blank? (str/split clean-text #"\s+"))
        cmd (first tokens)]
    (cond
      (str/blank? clean-text) {:action :leaderboard}
      (= "leaderboard" cmd)   {:action :leaderboard}
      (= "list" cmd)          (parse-list tokens)
      :else                   (parse-thanks tokens))))

(defn- response [blocks & {:keys [public?]}]
  {:response_type (if public? "in_channel" "ephemeral")
   :blocks        blocks})

(defn- leaderboard-blocks []
  (let [rows (map (fn [{:keys [rank user thanks streak]}]
                    [(str rank) user (str thanks) (str streak)])
                  (data/leaderboard))]
    (if (seq rows)
      [(slack/section "*Thank-ewe leaderboard*")
       (slack/table-block ["Rank" "Teammate" "Thanks" "Streak"] rows)
       (slack/context ["Table renders best on desktop. Type `/thank-ewe list date now` for today's feed."])]
      [(slack/section "No thanks recorded yet. Be the first with `/thank-ewe @teammate reason`.")])))

(defn- list-blocks [date]
  (let [entries (data/list-by-date date)
        target-date (data/normalize-date date)]
    (if (seq entries)
      [(slack/section (str "*Thanks for " target-date "*\n"
                           (str/join
                             "\n"
                             (map (fn [{:keys [from to reason]}]
                                    (str from " → " to ": " reason))
                                  entries))))]
      [(slack/section (str "No thanks recorded on " target-date ". Try another date or record one now."))])))

(defn- thank-blocks [command mention reason]
  (let [from (slack-user command)
        entry (data/record! {:from from :to mention :reason reason :date nil})]
    [(slack/section (str from " thanked " (:to entry) " for \"" (:reason entry) "\""))
     (slack/context ["Leaderboard updates automatically. Run `/thank-ewe leaderboard` to refresh."])]))

(defn process-command [command]
  (go
    (let [parsed (parse-command (:text command))]
      (case (:action parsed)
        :leaderboard (response (leaderboard-blocks) :public? true)
        :list-by-date (response (list-blocks (:value parsed)))
        :thank (response (thank-blocks command (:mention parsed) (:reason parsed)) :public? true)
        :invalid (response [(slack/section (:message parsed))])
        (response [(slack/section "Command not understood. Try `/thank-ewe leaderboard`.")] )))))

