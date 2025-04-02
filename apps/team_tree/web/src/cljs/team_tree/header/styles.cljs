(ns team-tree.header.styles
    (:require [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade]]))

(def header {:background-color (color :white)
             :font-family typography/title
             :border-bottom (str "1px solid " (->hex (color :grey)))
             :grid-area "title"
             :padding "0.7em"
             :padding-bottom "0"
             :background-image "url(/images/asurion.svg)"
             :background-size "auto 1.5em"
             :background-repeat "no-repeat"
             :background-position "0.8em 0.2em"
             :text-indent "6.8em"
             :font-size "2.0em"
             :text-transform "uppercase"
             :color (shade (color :grey) :lighter 33)})
