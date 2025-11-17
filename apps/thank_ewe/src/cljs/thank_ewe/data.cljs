(ns thank-ewe.data
  (:require [clojure.string :as str]))

(defn- today []
  (-> (js/Date.)
      (.toISOString)
      (subs 0 10)))

(defonce ^:private store
  (atom {:entries [{:id 1 :date "2025-11-01" :from "<@U111>" :to "<@U222>" :reason "Jumped in on pager duty."}
                   {:id 2 :date "2025-11-02" :from "<@U333>" :to "<@U222>" :reason "Unblocked the content sync."}
                   {:id 3 :date "2025-11-02" :from "<@U444>" :to "<@U555>" :reason "Shared the ranch dressing recipe."}
                   {:id 4 :date "2025-11-05" :from "<@U222>" :to "<@U333>" :reason "Pairing on the Rust migration."}
                   {:id 5 :date "2025-11-05" :from "<@U111>" :to "<@U555>" :reason "Late night release support."}]}))

(defn all-entries []
  (:entries @store))

(defn next-id []
  (inc (or (-> @store :entries last :id) 0)))

(defn normalize-date [value]
  (cond
    (nil? value) (today)
    (= "now" (str/lower-case value)) (today)
    :else value))

(defn record! [{:keys [from to reason date]}]
  (let [entry {:id (next-id)
               :from from
               :to to
               :reason reason
               :date (normalize-date date)}]
    (swap! store update :entries conj entry)
    entry))

(defn list-by-date [date]
  (let [target-date (normalize-date date)]
    (->> (all-entries)
         (filter #(= (:date %) target-date))
         (sort-by :id))))

(defn- longest-streak [entries]
  (->> entries
       (map :date)
       distinct
       count))

(defn leaderboard []
  (->> (all-entries)
       (group-by :to)
       (map (fn [[user entries]]
              {:user user
               :thanks (count entries)
               :streak (longest-streak entries)}))
       (sort-by (juxt (comp - :thanks) :user))
       (map-indexed (fn [idx entry]
                      (assoc entry :rank (inc idx))))))

