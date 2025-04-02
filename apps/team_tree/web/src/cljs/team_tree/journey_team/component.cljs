(ns team-tree.journey-team.component
  (:require [reagent.core :as r]
            [stylefy.core :refer [use-style sub-style]]
            [team-tree.journey-team.styles :as styles]
            [team-tree.journey-team.events]
            [team-tree.journey-team.subscriptions]
            [team-tree.journey-team.loading :as loading]
            [re-frame.core :as re-frame :refer [subscribe dispatch]]))

(defn- render-jt [style journey-team]
     [:div (use-style (styles/team-background (:radius journey-team)))
       [:div (use-style styles/label)
         (str (:name journey-team)) " Team"]
       [:div (use-style (style (:radius journey-team)))
         (:team journey-team)]])

(defn component[]
  (let [journey-team (subscribe [:team-tree.journey-team.subscriptions/journey-team])]
    (fn []
       (if (nil? (:name @journey-team))
         [loading/component]
         (render-jt styles/style-team @journey-team)))))
