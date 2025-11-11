(ns team-tree.person.metrics
    (:require
         [clojure.java.io :as io]
         [clojure.edn :as edn]
         [honeysql.helpers :as h]
         [honeysql.core :as hc]
         [repository.code-churn-db :refer [query-for]]))

(defn- load-code-metrics [github_id]
  (let [result (query-for (-> (h/select [:github_id :contributions
                                           (h/from :contributors)
                                           (h/where := :contributors/github_id github_id)
                                           (hc/format :namespace-as-table? true)])))]))

(defn resolve-code-metrics
  [context args value]
  (let [{:keys [github_id]} value]
    (if github_id

      {:repository_count (count (query-for (-> (h/select [:github_id :contributions])
                                               (h/from :contributors)
                                               (h/where := :contributors/github_id github_id)
                                               (hc/format :namespace-as-table? true))))})))
