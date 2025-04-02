(ns team-tree.role.styles
  (:require [stylefy.core :as stylefy]
            [themes.typography :as typography]
            [themes.palette :refer [color shade]]
            [team-tree.constants :refer [css-vendors]]))

(def style (merge
             {:width "56px"
              :height "56px"
              :background-size "100% 100%"
              :background-repeat "no-repeat"
              :background-position "top left"
              :top "0"
              :left "0"
              :transition "all 1s linear"
              :position "absolute"}
             {::stylefy/sub-styles
               {:label {
                        :display "block"
                        :width "102%"
                        :text-transform "capitalize"
                        :position "absolute"
                        :bottom "-7px"
                        :font-size "10px"
                        :color (shade (color :hero-blue) :lighter 30)
                        :text-align "center"}}}))


(def icons ["role-icons-01.svg"
            "role-icons-02.svg"
            "role-icons-03.svg"
            "role-icons-04.svg"
            "role-icons-05.svg"
            "role-icons-06.svg"
            "role-icons-07.svg"])

(defn- generate-icon []
  (let [icon (rand-int (count icons))]
    (get icons icon)))

(defn random-icon []
   (let [image (generate-icon)]
     {:background-image (str "url(/images/" image ")")}))
