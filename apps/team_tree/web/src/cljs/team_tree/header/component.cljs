(ns team-tree.header.component
  (:require [reagent.core :as r]
            [stylefy.core :refer [use-style]]
            [team-tree.header.styles :as styles]
            [team-tree.header.subscriptions]
            [re-frame.core :as re-frame :refer [subscribe dispatch]]))

(defn component[]
  (let [hello (subscribe [:team-tree.header.subscriptions/hello])]
    (fn []
      [:header (use-style styles/header)
        @hello])))
