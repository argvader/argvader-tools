(ns team-tree.view.journey-team
  (:require [router.core :refer [query-params]]
            [team-tree.header.component :as header]
            [team-tree.navigation.component :as navigation]
            [team-tree.journey-team.events :as events]
            [team-tree.journey-team.component :as jt]
            [team-tree.layout :refer [apply-layout]]))

(defn journey-team-view [props]
  (let [team-id (get-in props [:route-params :id])]
    (events/find-team-event team-id)
    (fn []
      (apply-layout
        ^{:key "navigation"}[navigation/component]
        ^{:key "header"}[header/component]
        ^{:key "team"}[jt/component]))))
