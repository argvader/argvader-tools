(ns team-tree.journey-team.resolver
  (:require
       [clojure.java.io :as io]
       [clojure.edn :as edn]
       [honeysql.helpers :as h]
       [honeysql.core :as hc]
       [repository.team-tree-db :refer [query-for find-one]]))

(defn resolve-team-by-id
  [context args value]
  (let [{:keys [id]} args]
    (find-one (-> (h/select :id :name :nick_name :domain_id)
                  (h/from :teams)
                  (h/where := :id id)
                  (hc/format)))))

(defn resolve-teams-by-domain
  [context args value]
  (let [{:keys [id]} value]
    (query-for (-> (h/select :id :name :nick_name :domain_id)
                   (h/from :teams)
                   (h/where := :domain_id id)
                   (hc/format)))))

(defn resolve-team-members
  [context args value]
  (let [{:keys [id]} value]
    (query-for (-> (h/select :id :person_id :team_id :role)
                   (h/from :team_members)
                   (h/where := :team_id id)
                   (hc/format)))))

(defn resolver-map
  []
  {:query/team-by-id resolve-team-by-id
   :resolver/team-members resolve-team-members
   :resolver/teams resolve-teams-by-domain})
