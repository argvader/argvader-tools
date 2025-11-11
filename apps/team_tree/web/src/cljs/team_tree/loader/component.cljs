(ns team-tree.loader.component
  (:require [stylefy.core :refer [use-style use-sub-style]]
            [team-tree.loader.styles :as styles]))

(defn component []
  [:div (use-style styles/loader)
    "Loading"
    [:div (use-sub-style styles/loader :ring1)]
    [:div (use-sub-style styles/loader :ring2)]
    [:div (use-sub-style styles/loader :ring3)]
    [:div (use-sub-style styles/loader :ring4)]
    [:div (use-sub-style styles/loader :ring5)]])
