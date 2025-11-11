(ns code-churn.gather.repository
  (:require [clojure.string :refer [join]]
            [repository.code-churn-db :as db]
            [honeysql.helpers :as h]
            [honeysql.core :as hc]))

(def repo-ids (atom []))

(defn load-ids []
  (reset! repo-ids
    (db/query-for (-> (h/select :id)
                      (h/from :repositories)
                      (hc/format)))))

(defn- build-statement [repository]
  (let [found (filter #(= (:id repository) (:id %)) @repo-ids)]
    (if (empty? found)
      (-> (h/insert-into :repositories)
          (h/values [repository])
          (hc/format))
      (-> (h/update :repositories)
          (h/sset repository)
          (h/where [:= :id (:id repository)])
          (hc/format)))))

(defn- upsert-repositories! [repositories]
  (run! #(db/query-for (build-statement %)) repositories))

(defn transform-languages [repo]
  (let [languages (get-in repo [:languages :nodes])]
    (merge repo  {:languages (join "," (map #(:name %) languages))})))

(defn remove-collaborators [repo]
   (dissoc repo :collaborators))

(defn transform-response [response]
   (map #(-> %
             (remove-collaborators)
             (transform-languages)) response))


(defn store-repositories [data page-info]
    (println (str "Processing " (count (get-in data [:data :organization :repositories :nodes])) " Repositories after:" (:endCursor page-info)))
    (-> data
        (get-in [:data :organization :repositories :nodes])
        (transform-response)
        (upsert-repositories!)))
