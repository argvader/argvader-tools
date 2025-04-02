(ns team-tree.person.data
  (:require
       [clojure.java.io :as io]
       [clojure.edn :as edn]
       [honeysql.helpers :as h]
       [honeysql.core :as hc]
       [repository.team-tree-db :refer [find-one]]))

(defn- find-person-by-id [id]
  (if (some? id)
    (find-one (-> (h/select :id :job_level :first_name :last_name :email :github_id)
                  (h/from :persons)
                  (h/where := :id id)
                  (hc/format)))))

(defn resolve-person-by-id
  [context args value]
  (let [{:keys [id]} args]
    (find-person-by-id id)))

(defn resolve-person-by-product-lead
  [context args value]
  (let [{:keys [product_lead_id]} value]
    (find-person-by-id product_lead_id)))

(defn resolve-person-by-design-lead
  [context args value]
  (let [{:keys [design_lead_id]} value]
    (find-person-by-id design_lead_id)))

(defn resolve-person-by-engineering-lead
  [context args value]
  (let [{:keys [engineering_lead_id]} value]
    (find-person-by-id engineering_lead_id)))

(defn resolve-person-by-team-member
  [context args value]
  (let [{:keys [person_id]} value]
    (find-person-by-id person_id)))
