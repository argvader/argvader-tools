(ns web.css
  (:require [garden.types #?@(:cljs [:refer [CSSFunction CSSAtRule]])]
            [garden.util :as util]
            [garden.core]
            [garden.color :refer [lighten darken]]
            [garden.stylesheet :refer [at-media]])
  #?(:clj
      (:import garden.types.CSSFunction
               garden.types.CSSAtRule)))

(defmacro defbreakpoint [name media-params]
  `(defn ~name [& rules#]
     (at-media ~media-params
       [:& rules#])))
