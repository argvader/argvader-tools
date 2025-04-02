(ns team-tree.loader.styles
    (:require [stylefy.core :as stylefy]
              [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade]]))

(stylefy/keyframes "spinspin"
                   ["0%"
                    {:transform "rotate(0deg)"}]
                   ["50%"
                    {:transform "rotate(180deg)"}]
                   ["100%"
                    {:transform "rotate(360deg)"}])

(def ring
  {:width "140px"
   :height "140px"
   :border-radius "100%"
   :position "absolute"
   :border (str "1px solid " (->hex (color :asurion-purple)))
   :animation "spinspin 1s infinite"
   :transition "2s"
   :border-bottom "none"
   :border-right "none"
   :animation-timing-function "linear"
   :margin-left "-60px"
   :margin-top "-80px"})



(def loader
  {:grid-area "content"
   :display "grid"
   :position "relative"
   :font-size "14px"
   :color (->hex (color :asurion-purple))
   ::stylefy/sub-styles {:ring1 (merge ring {})
                         :ring2 (merge ring {:animation-delay "0.3s"
                                             :margin-left "-53px"
                                             :margin-top "-72px"
                                             :width "130px"
                                             :height "130px"})
                         :ring3 (merge ring {:animation-delay "0.5s"
                                             :margin-left "-44px"
                                             :margin-top "-60px"
                                             :width "120px"
                                             :height "120px"})
                         :ring4 (merge ring {:animation-delay "0.7s"
                                             :margin-left "-36px"
                                             :margin-top "-53px"
                                             :width "110px"
                                             :height "110px"})
                         :ring5 (merge ring {:animation-delay "0.9s"
                                             :margin-left "-25px"
                                             :margin-top "-48px"
                                             :width "100px"
                                             :height "100px"})}})
