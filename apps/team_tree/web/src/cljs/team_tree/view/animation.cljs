(ns team-tree.view.animation
  (:require [rx-animation.demo.simple :as simple]
            [rx-animation.demo.easing :as easing]
            [rx-animation.demo.events :as events]
            [rx-animation.demo.color :as color]))

; This is just a temp for testing animation lib
; When this gets bigger - I'll get a new subdomain for the demo app with docs.
(defn demo-view []
  (fn []
    [:div {:style {:padding "0.8rem"}}
      [:h1 "This is the Animation Demo Page."]
      [:h3 "First Simple demo"]
      [simple/demo]
      [:h3 "Easing examples"]
      [easing/demo]
      [:h3 "Color Tests"]
      [color/demo]
      [:h3 "Animations Events"]
      [events/demo]]))
