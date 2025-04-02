(ns functions
  (:require [ranchify.handler :as ranchify]))

(defn exports []
  #js {:ranchify ranchify/handler})
