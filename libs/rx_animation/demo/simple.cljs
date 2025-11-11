(ns rx-animation.demo.simple
  (:require [rx-animation.core :as animation]
            [rx-animation.component :as animated]
            [rx-animation.timeline :as timeline]
            [stylefy.core :refer [use-style sub-style]]
            [stylefy.core :as stylefy]))

(defn q [selector] (.querySelectorAll js/document selector))

(def animatable {:width "100px"
                 :height "100px"
                 :float "left"
                 :margin "4px"
                 :background "blue"})

(defn animate-location [frame]
  (let [targets (q ".demo1")]
    (run! #(-> %
               (.-style)
               (.-transform)
               (set! (str "translateX(" (* 200 frame) "px) rotate(" (* 720 frame) "deg"))) targets)))

(defn animate-radius [frame]
  (let [targets (q ".demo1")]
    (run! #(-> %
               (.-style)
               (.-borderRadius)
               (set! (str (* 50 frame) "px"))) targets)))

(animation/create :demo1
 {:timeline (->> (timeline/duration 3500)
                 (timeline/with-ease :elastic-in-out)
                 (timeline/looping))
  :on-frame (fn [frame]
                (animate-location frame)
                (animate-radius frame)
                {})})

(defn demo []
  (fn []
    [:div {:style {:height "130px"}}
      [animated/component {:animation :demo1}
        [:div (use-style animatable {:class "demo1"})]]]))
