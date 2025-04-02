(ns team-tree.domain.resolver
  (:require
       [clojure.java.io :as io]
       [clojure.edn :as edn]
       [honeysql.helpers :as h]
       [honeysql.core :as hc]
       [repository.team-tree-db :refer [query-for find-one]]))

(defn resolve-domain-by-id
  [context args value]
  (let [{:keys [id]} args]
    (find-one (-> (h/select :id :name :product_lead_id :design_lead_id :engineering_lead_id)
                  (h/from :domains)
                  (h/where := :id id)
                  (hc/format)))))

(defn resolve-domains
  [context args value]
  (query-for (-> (h/select :id :name :product_lead_id :design_lead_id :engineering_lead_id)
                 (h/from :domains)
                 (hc/format))))

(defn resolver-map
  []
  {:query/domains resolve-domains
   :query/domain-by-id resolve-domain-by-id})
