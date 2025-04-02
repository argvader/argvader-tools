(ns web.error-boundary
  (:require [reagent.core :as r]
            [stylefy.core :refer [use-style sub-style]]))

(defn- err-boundary-component
  [view & children]
  (let [err-state (r/atom nil)]
    (r/create-class
      {:display-name "ErrorBoundary"
       :component-did-catch (fn [err info]
                              (reset! err-state [err info]))
       :reagent-render (fn [& children]
                         (if (nil? @err-state)
                           (into [:<>] children)
                           (let [[_ info] @err-state]
                             [view info])))})))

(defn error-boundary [view]
  (partial err-boundary-component view))
