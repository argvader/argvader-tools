(ns functions
  (:require [contentful.handler :as contentful]))

(defn exports []
  #js {:contentful contentful/handler})
