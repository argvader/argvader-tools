(ns team-tree.view.organization
  (:require   [re-frame.core :as re-frame]
              [stylefy.core :as stylefy]
              [team-tree.layout :refer [apply-layout]]
              [team-tree.header.component :as header]
              [team-tree.navigation.component :as navigation]
              [team-tree.organization.component :as organization]))

(defn organization-view []
  (fn []
    (apply-layout
      ^{:key "navigation"}[navigation/component]
      ^{:key "header"}[header/component]
      ^{:key "organization"}[organization/component])))
