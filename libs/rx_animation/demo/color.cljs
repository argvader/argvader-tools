(ns rx-animation.demo.color
  (:require [rx-animation.core :as animation]
            [rx-animation.component :as animated]
            [rx-animation.timeline :as timeline]
            [rx-animation.color :refer [tween]]
            [themes.palette :refer [color]]
            [stylefy.core :refer [use-style sub-style]]
            [stylefy.core :as stylefy]))

(defn q [selector] (.querySelectorAll js/document selector))

(def animatable {:width "100px"
                 :height "100px"
                 :float "left"
                 :margin "4px"
                 :border-radius "50"
                 :background "blue"})

(def tween-color (tween (color :asurion-purple) (color :asurion-green)))

(defn animate-position [frame]
  (let [targets (q ".colorDemo")]
    (run! #(-> %
               (.-style)
               (.-transform)
               (set! (str "translateX(" (* 200 frame) "px"))) targets)))


(defn animate-color [frame]
  (let [targets (q ".colorDemo")]
    (run! #(-> %
               (.-style)
               (.-background)
               (set! (tween-color frame))) targets)))

(animation/create :color-demo {:timeline (->> (timeline/duration 3500)
                                              (timeline/bouncing))
                               :on-frame (fn [frame]
                                             (animate-color frame)
                                             (animate-position frame)
                                             {})})
(defn demo []
  (fn []
    [:div {:style {:height "130px"}}
      [animated/component {:animation :color-demo}
        [:div (use-style animatable {:class "colorDemo"})]]]))
