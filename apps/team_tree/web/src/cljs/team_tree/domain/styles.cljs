(ns team-tree.domain.styles
    (:require [clojure.string :refer [join]]
              [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade]]))

(def domain
  {:font-family typography/basic
   :grid-area "content"
   :padding "0.7em 2.0em"})

(defn teams [max-team-size number-of-teams]
  (let [columns (join " " (take number-of-teams (repeat "1fr")))
        rows (join " " (take max-team-size (repeat "1fr")))]
    {:display "grid"
     :grid-auto-flow "column"
     :grid-template-columns columns
     :grid-template-rows rows
     :background (shade (color :grey) :lighter 25)
     :gap "1px 1px"
     :padding "1px"}))

(def title
  {:background (->hex (color :asurion-purple))
   :color (->hex (color :white))
   :padding "0.5em 0.3em"})

(def block
  {:padding "0.3em"
   :background (->hex (color :white))})

(def loading
  {:display "grid"
   :margin 0
   :padding 0
   :width "100%"
   :height "100%"})

(def leaders-container
  {:display "flex"
   :margin-bottom "0.7em"})

(def leader-role
  {:position "absolute"
   :font-size "0.7rem"})


(def leader
  {:flex-grow "1"
   :position "relative"
   :padding "0.5em 0.3em"})
