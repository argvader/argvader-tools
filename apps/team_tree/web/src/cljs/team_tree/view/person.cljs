(ns team-tree.view.organization
  (:require [router.core :as router]))

(defn person-view []
  (fn []
    [:div "This is the Person Page."
     [:div [:a {:href (router/url-for :home)} "go to Home Page"]]]))
