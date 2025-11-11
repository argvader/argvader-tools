(ns team-tree.journey-team.model
    (:require [team-tree.role.utils :refer [roles]]))

(defn filter-role [role-name members]
  (let [list (filter #(= (:role %) role-name) members)]
     (if (empty? list)
        []
        (vec list))))

(defn- collect-members [members]
  (let [result (flatten (vec (map #(filter-role % members) roles)))]
     result))

(defprotocol TeamInterface
  (team-id [this])
  (team-name [this])
  (team-members [this]))

(deftype JourneyTeam [team-map]
  TeamInterface
  (team-id [this] (:id team-map))
  (team-name [this] (:name team-map))
  (team-members [this]
    (collect-members (:members team-map))))
