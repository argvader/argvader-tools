(ns team-tree.organization.visualization.styles
    (:require [stylefy.core :as stylefy]
              [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade]]))

(def graph
  {::stylefy/mode {:hover {:fill "rgb(98, 131, 213)"}}
   :fill (->hex (color :asurion-blue))
   :stroke (shade (color :grey) :lighter 45)})
