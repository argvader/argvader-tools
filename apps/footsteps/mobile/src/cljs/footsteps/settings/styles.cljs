(ns footsteps.settings.styles
  (:require [themes.typography :as typography]
            [themes.palette :refer [color shade ->hex]]))

(def container
  {:flex 1
   :backgroundColor (->hex (color :white))
   :alignItems "center"
   :justifyContent "center"})

(def panel
  {:flex 1
   :backgroundColor (->hex (color :white))
   :position "relative"})

(def panel-header
  {:height 48
   :backgroundColor (->hex (color :hero-blue))
   :justifyContent "flex-end"
   :padding 8})

(def text-header
  {:fontSize 22
   :paddingLeft 54
   :color (->hex (color :white))})

(def category
  {:fontSize 18
   :color (->hex (color :near-black))})

(def icon
  {:alignItems "center"
   :justifyContent "center"
   :position "absolute"
   :top 8
   :left 8
   :width 32
   :height 32
   :zIndex 1})

(def icon-background
  {:backgroundColor (->hex (color :white))
   :position "absolute"
   :top 0
   :left 8
   :width 48
   :height 48
   :borderRadius 24
   :zIndex 1})
