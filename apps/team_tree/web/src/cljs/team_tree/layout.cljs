(ns team-tree.layout
    (:require
        [stylefy.core :refer [use-style]]))

(def grid
    {:display "grid"
     :height "inherit"
     :width "inherit"
     :grid-gap "10px"
     :grid-template-columns "100%"
     :grid-template-rows "70px 1fr 80px"
     :grid-template-areas "\"title\" \n\"content\" \n\"tools\""})

(defn apply-layout
  [& components]
  [:div (use-style grid)
     components])
