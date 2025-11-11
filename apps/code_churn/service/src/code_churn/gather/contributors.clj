(ns code-churn.gather.contributors
  (:require [clojure.string :refer [join]]
            [repository.code-churn-db :as db]
            [git-api.core :as github]
            [honeysql.helpers :as h]
            [honeysql.core :as hc]))

(defn- load-contributors [repo]
  (db/query-for (-> (h/select :github_id)
                    (h/from :contributors)
                    (h/where [:= :repository_id (:id repo)])
                    (hc/format))))

(defn- reduce-weeks [weeks]
  {:a (reduce + 0 (map :a weeks))
   :d (reduce + 0 (map :d weeks))
   :c (reduce + 0 (map :c weeks))})

(defn- save-contributor! [contributor current-contributors repo]
  (let [found (filter #(= (get-in contributor [:author :login]) (:github_id %)) current-contributors)
        data (reduce-weeks (:weeks contributor))]
    (if (empty? found)
      (db/query-for
         (-> (h/insert-into :contributors)
             (h/values [{:github_id (get-in contributor [:author :login]) :repository_id (:id repo) :contributions (:total contributor) :adds (:a data) :deletes (:d data) :commits (:c data)}])
             (hc/format)))
      (db/query-for
         (-> (h/update :contributors)
             (h/sset {:contributions (:total contributor) :adds (:a data) :deletes (:d data) :commits (:c data)})
             (h/where [:= :repository_id (:id repo)] [:= :github_id (get-in contributor [:author :login])])
             (hc/format))))))

(defn- process-contributors! [repo]
  (let [current-contributors (load-contributors repo)
        contributors (github/find-contributors "Soluto-Private" repo)]
    (println (str "number of contributors: " (count contributors)))
    (run! #(save-contributor! % current-contributors repo) contributors)))


(defn store-contributors [data]
    (println "Processing Contributors...")
    (let [repos (get-in data [:data :organization :repositories :nodes])]
      (run! process-contributors! repos)))
