(ns rx-animation.component
  (:require [reagent.core :as r]
            [stylefy.core :refer [use-style sub-style]]
            [rx-animation.core :as animation]
            ["react" :as react]))

(def styles {:position "absolute"})

(defn component [props & children]
  (r/create-class
    {:component-did-mount
     (fn []
       (animation/play (:animation props)))

     :display-name "animated"

     :reagent-render
     (fn [props children]
       [:<>
         children])}))
