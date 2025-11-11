(ns code-churn.gather.github
  (:require [clojure.string :refer [join]]
            [git-api.core :as github]
            [code-churn.gather.repository :as repo]
            [code-churn.gather.contributors :as contributors]))

(def query "organizations ($organization: String!) { organization (login: $organization) {
 name
 repositories(first: 50 %s) {
   pageInfo {
     hasNextPage
     endCursor
   }
   nodes {
     name
     id
     description
     url
     languages (first: 12) {
       nodes {
         name
       }
     }
   }
 }
}}")

(defn build-query
  ([organization after]
   (if (nil? after)
     (format query "")
     (format query (str "after: \"" after "\""))))
  ([organization]
   (build-query organization nil)))


(defn batch-data
  ([organization]
   (batch-data organization nil))
  ([organization after]
   (println "Processing Batch ....")
   (let [data (github/query-sync
                (build-query organization after)
                {:organization organization})
         page-info (get-in data [:data :organization :repositories :pageInfo])]
     (repo/store-repositories data page-info)
     (contributors/store-contributors data)
     (if (:hasNextPage page-info)
       (batch-data organization (:endCursor page-info))))))

(defn process-all-data [organization]
  (repo/load-ids)
  (batch-data organization))
