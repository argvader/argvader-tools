(ns functions
  (:require [acronym.handler :as acronym]))

(defn exports []
  #js {:acronym acronym/handler})
