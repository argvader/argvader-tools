(ns team-tree.view.design-system
  (:require [cljs.js]
            [cljs-css-modules.macro :refer-macros [defstyle]]
            [stylefy.core :refer [use-style]]
            [themes.typography :as typography]
            [themes.palette :as palette]
            [team-tree.constants :refer [css-vendors]]))

(defstyle style css-vendors
  [".container"
    {:padding "1.5em"}
    ["header"
      {:font-size "5.0em"
       :font-weight "bold"}]])

(defn- expand-color [color shade]
    (let [base (get palette/colors color)
          background (palette/shade base :lighter shade)]
      [:div {:key (str (palette/->hex base) shade)
             :style {:text-align "center"
                     :padding "0.3em"
                     :color (palette/->hex (palette/invert background))
                     :background-color (palette/->hex background)}}
        (cond
           (zero? shade) color
           :else shade)]))

(defn- abs [n] (max n (- n)))

(defn- show-palette []
  (let [colors (keys palette/colors)
        col-count (count colors)]
     [:section {:style
                  {:margin "2.0em"
                   :display "grid"
                   :grid-gap "10px"
                   :grid-template-columns (str "repeat(" col-count ", 1fr)")
                   :grid-template-rows "100px)"}}
       (for [shade (vec (range 90 -100 -10))]
          (doall (map #(expand-color % shade) colors)))]))


(defn design-system-view []
  [:div {:class-name (:container style)}
    [:header
       "Design System"]
    [:h1
      "Palette"]
    (show-palette)])
