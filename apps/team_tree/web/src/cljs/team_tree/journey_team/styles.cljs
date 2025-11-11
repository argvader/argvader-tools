(ns team-tree.journey-team.styles
   (:require [stylefy.core :as stylefy]
             [themes.typography :as typography]
             [themes.palette :refer [color shade]]
             [team-tree.constants :refer [css-vendors]]))

(stylefy/keyframes "spin"
                   [:from
                    {:transform "rotate(0deg)"}]
                   [:to
                    {:transform "rotate(-360deg)"}])

(defn team-background [radius]
  {
   :position "relative"
   :margin "30px 20px"
   :height (str (* 2 radius) "px")
   :width (str (* 2 radius ) "px")
   :border-radius (str radius "px")
   :align-items "center"
   :grid-area "content"
   :background-color (shade (color :grey) :lighter 85)})

(defn style-team [radius]
  {
   :position "relative"
   :height (str (* 2 radius) "px")
   :width (str (* 2 radius ) "px")
   :border-radius (str radius "px")
   :display "flex"
   :justify-content "center"
   :align-items "center"
   :grid-area "content"})

(def label
   {:text-align "center"
    :position "absolute"
    :width "100%"
    :top "50%"})
