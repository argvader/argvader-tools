(ns footsteps.styles
   (:require [themes.typography :as typography]
             [themes.palette :refer [color shade ->hex]]))

(def container
  {:flex 1
   :backgroundColor (->hex (color :white))
   :alignItems "center"
   :justifyContent "center"})

(def title
  {:fontWeight "bold"
   :fontSize 24
   :color (->hex (color :steel-blue))})

(def button-container
  {:flex 1})
