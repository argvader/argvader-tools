(ns functions
  (:require [thank-ewe.handler :as thank-ewe]))

(defn exports []
  #js {:thankEwe thank-ewe/handler})
