(ns rx-animation.demo.events
  (:require [rx-animation.core :as animation]
            [rx-animation.component :as animated]
            [rx-animation.timeline :as timeline]
            [rx-animation.color :refer [tween]]
            [reagent.core :as r]
            [themes.palette :refer [color]]
            [stylefy.core :refer [use-style sub-style]]
            [stylefy.core :as stylefy]))

(def animation-data {
                     :width "100%"
                     :height "100px"
                     :margin "4px"
                     :padding "9px"
                     :background "#ccc"})

(def frame-count (r/atom 0))
(def status (r/atom "Stopped"))

(def animation (animation/create
                 {:on-frame (fn [frame]
                              (swap! frame-count inc))
                  :on-start (fn []
                              (reset! status "Start"))
                  :on-end (fn []
                            (reset! status "Ended"))}))

(defn handle-click [name]
  (fn [event]
    (reset! frame-count 0)
    (reset! status "Playing")
    (animation/play name)))

(defn demo []
  (fn []
    (let [name (:name animation)]
      [:div (use-style animation-data)
        [:button {:on-click (handle-click name)} "Play"]
        [:table
           [:tbody
             [:tr
                [:td "Name: "]
                [:td name]]
             [:tr
                [:td "Status: "]
                [:td @status]]
             [:tr
                [:td "Frame: "]
                [:td @frame-count]]]]])))
