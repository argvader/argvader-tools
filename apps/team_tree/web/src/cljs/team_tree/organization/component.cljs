(ns team-tree.organization.component
  (:require
            [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [stylefy.core :refer [use-style]]
            [team-tree.loader.component :as loader]
            [team-tree.organization.styles :as styles]
            [team-tree.organization.events]
            [team-tree.organization.subscriptions]
            [team-tree.domain.utils :refer [domain-size]]
            [team-tree.organization.visualization.component :as graph]))

(defn- find-sizes
  [organization]
  (map #(assoc % :size (domain-size %)) organization))

(defn component[]
  (let [organization (subscribe [:team-tree.organization.subscriptions/organization])
        resized (subscribe [:team-tree.organization.subscriptions/resized])]
    (dispatch [:team-tree.organization.events/on-resize])
    (fn []
       (let [domains (find-sizes @organization)]
         [:div (use-style styles/organization {:id "graph"})
           (if (seq domains)
             ^{:key @resized}[graph/component (reverse (sort-by :size domains))]
             [loader/component])]))))
