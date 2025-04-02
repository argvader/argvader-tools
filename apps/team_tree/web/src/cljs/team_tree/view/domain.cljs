(ns team-tree.view.domain
  (:require [router.core :refer [query-params]]
            [team-tree.header.component :as header]
            [team-tree.domain.component :as domain]
            [team-tree.navigation.component :as navigation]
            [team-tree.domain.events :as events]
            [team-tree.layout :refer [apply-layout]]))

(defn domain-view [props]
  (let [domain-id (get-in props [:route-params :id])]
    (events/find-domain-event domain-id)
    (fn []
      (apply-layout
        ^{:key "navigation"}[navigation/component]
        ^{:key "header"}[header/component]
        ^{:key "domain"}[domain/component]))))
