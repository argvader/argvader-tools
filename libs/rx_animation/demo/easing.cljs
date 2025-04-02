(ns rx-animation.demo.easing
  (:require
           [clojure.string :refer [split lower-case capitalize join]]
           [rx-animation.core :as animation]
           [rx-animation.component :as animated]
           [rx-animation.timeline :as timeline]
           [themes.typography :as typography]
           [themes.palette :refer [color shade]]
           [team-tree.constants :refer [css-vendors]]
           [stylefy.core :refer [use-style sub-style]]
           [stylefy.core :as stylefy]))

(defn q [selector] (.querySelectorAll js/document selector))

(def ease-labels {:float "left"
                  :line-height "10px"})

(def ease-demo {:margin-left "100px"
                :width "70%"
                :height "660px"
                :position "relative"
                :padding-top "6px"
                :margin-bottom "27px"
                :background-color (shade (color :grey) :lighter 85)})

(def scrubber {:position "absolute"
               :width "1px"
               :height "100%"
               :top "0"
               :left "0"
               :background-color (color :grey)})

(def ball {:position "relative"
           :width "22px"
           :height "15px"
           :margin "10px 0 11px 0"
           :background-color (color :asurion-purple)
           :border-radius "11px"})

(def easing-fns timeline/all-easing-fns)

(defn camel-case [input-string]
  (let [sanitized (name input-string)
        words (split sanitized #"[\s_-]+")]
    (join "" (cons (lower-case (first words)) (map capitalize (rest words))))))

(def ease-timeline (->> (timeline/duration 4000)
                        (timeline/looping)))


(defn apply-ease-demo [ease class]
  (let [timeline (timeline/with-ease ease ease-timeline)
        animate-fn (fn [frame]
                     (let [targets (q class)]
                       (run! #(-> %
                                  (.-style)
                                  (.-left)
                                  (set! (str "calc(" (* 100 frame) "% - 11px)"))) targets)))]
    (animation/create ease
      {:timeline timeline
       :on-frame (fn [frame]
                   (animate-fn frame)
                  {})})))

(defn animate-scrubber [frame]
  (let [targets (q ".scrubber")]
    (run! #(-> %
               (.-style)
               (.-left)
               (set! (str (* 100 frame) "%"))) targets)))

(animation/create :ease-demo
  {:timeline ease-timeline
   :on-frame (fn [frame]
               (animate-scrubber frame)
               {})})

(run! #(apply-ease-demo % (str "." (camel-case %))) easing-fns)

(defn demo []
  (fn []
    [:div {:style {:overflow-x "hidden"}}
      [:div (use-style ease-labels)
        (for [ease easing-fns]
          [:p (camel-case ease)])]
      [:div (use-style ease-demo)
        [animated/component {:animation :ease-demo}
          [:div (use-style scrubber {:class "scrubber"})]]
        (for [ease easing-fns]
          [animated/component {:animation ease}
            [:div (use-style ball {:class (camel-case ease)})]])]]))
