(ns footsteps.sfx.styles
   (:require [themes.typography :as typography]
             [themes.palette :refer [color shade ->hex]]))

(def container
  {:flex 1
   :backgroundColor (->hex (color :white))
   :alignItems "center"
   :justifyContent "center"})

(def label
  {:fontWeight "normal"
   :fontSize 15
   :color (->hex (color :steel-blue))})

(def button
  {:backgroundColor (->hex (color :steel-blue))
   :color (->hex (color :white))
   :padding 9
   :marginBottom 9
   :alignSelf "stretch"})

(def active
  {:backgroundColor (->hex (shade (color :steel-blue) :lighter 15))})

(def button-text
  {:color (->hex (color :white))})
