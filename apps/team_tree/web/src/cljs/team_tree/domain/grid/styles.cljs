(ns team-tree.domain.grid.styles
    (:require [clojure.string :refer [join]]
              [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade tint]]
              [team-tree.role.utils :refer [roles]]))

(defmulti format-role (fn [team] (get team :role)))

(defmethod format-role "engineer" [params]
  {:color (shade (color :asurion-green) :darker 60)
   :background (->hex (color :asurion-green))})

(defmethod format-role "tech lead" [params]
  {:color (shade (color :dirt) :darker 60)
   :background (shade (color :dirt) :lighter 33)})

(defmethod format-role "UX lead" [params]
  {:color (shade (color :hero-blue) :darker 60)
   :background (shade (color :hero-blue) :lighter 33)})

(defmethod format-role "product lead" [params]
  {:color (shade (color :hero-red) :darker 60)
   :background (shade (color :hero-red) :lighter 40)})

(defmethod format-role "data science" [params]
  {:color (shade (color :asurion-green) :darker 60)
   :background (shade (color :asurion-green) :lighter 50)})

(defmethod format-role :default [params]
  {:color (->hex (color :white))
   :background (->hex (color :white))})

(def link
  {:text-decoration "none"})

(def title
  {:background (->hex (color :asurion-purple))
   :color (->hex (color :white))
   :text-align "center"
   :height "100%"
   :padding "0.5em 0.3em"})

(defn block [team]
  (let [format (format-role team)]
    {:padding "1.0em"
     :position "relative"
     :text-align "center"
     :color (:color format)
     :background-color (:background format)}))

(defn level [team]
 (let [display (if (get-in team [:person :job_level]) "inline" "none")
       format (format-role team)]
   {:position "absolute"
    :bottom "0.2em"
    :right "0.2em"
    :opacity "0.7"
    :font-size "0.3rem"
    :display display
    :text-transform "uppercase"
    :color (shade (:background format) :darker 25)
    :list-item {:color "black"}}))

(defn role [team]
  (let [format (format-role team)]
   {:position "absolute"
    :bottom "0.2em"
    :right "0.2em"
    :opacity "0.7"
    :font-size "0.4rem"
    :text-transform "uppercase"
    :color (tint (:background format) :darker 45)
    :list-item {:color "black"}}))

(def metrics
  {
   :position "absolute"
   :bottom "0.2em"
   :left "0.2em"
   :font-size "0.3em"
   :text-indent "4.4em"
   :text-align "left"
   :line-height "4em"
   :width "14em"
   :height "4em"
   :background-image "url(/images/github.svg)"
   :background-repeat "no-repeat"
   :background-size "contain"})
