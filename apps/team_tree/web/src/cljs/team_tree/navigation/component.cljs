(ns team-tree.navigation.component
  (:require [stylefy.core :refer [use-style]]
            [router.core :refer [url-for]]
            [re-frame.core :as re-frame :refer [subscribe]]
            [team-tree.navigation.styles :as styles]))

(defn component []
  (fn []
    [:div (use-style styles/zoom)
      [:div [:a {:href (url-for :home)} "Team Tree"]]]))
