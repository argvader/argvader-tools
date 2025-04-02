(ns team-tree.schema
  (:require
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.util :as util]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.stuartsierra.component :as component]
    [clojure.edn :as edn]
    [team-tree.domain.resolver :as domain]
    [team-tree.journey-team.resolver :as journey-team]
    [team-tree.person.resolver :as person]))


(defn resolver-map
  [component]
  (merge (domain/resolver-map)
         (journey-team/resolver-map)
         (person/resolver-map)))

(defn load-schema
  [component]
  (-> (io/resource "team-tree-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]
  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (map->SchemaProvider {})})
