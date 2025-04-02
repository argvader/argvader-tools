(ns themes.palette
  (:require [garden.color :refer [rgb lighten darken as-hex hex->rgb]]))

(def steps 10)

(defn- compute-range [start-value end-value select]
  (let [step (/ (- end-value start-value) steps)]
    (-> (+ (* select step) start-value)
        (max 0)
        (min 255)
        (int))))

(defn- calculate-shade [start-color end-color select]
  (let [r (compute-range (:red start-color) (:red end-color) select)
        g (compute-range (:green start-color) (:green end-color) select)
        b (compute-range (:blue start-color) (:blue end-color) select)]
    (rgb r g b)))

(defn shade [color direction step]
  (let [select  (int (/ step 10))]
    (case direction
      :lighter (calculate-shade color (rgb 255 255 255) select)
      :darker (calculate-shade color (rgb 0 0 0) select))))

(defn tint [color direction step]
  (case direction
    :lighter (lighten color step)
    :darker (darken color step)))

(def colors {
             :asurion-purple (rgb 130 35 210)
             :asurion-green (rgb 110 250 195)
             :asurion-blue (rgb 107 139 255)
             :asurion-yellow (rgb 210 250 70)
             :grey (rgb 120 120 120)
             :steel-blue (rgb 43 69 92)
             :hero-blue (rgb 30 157 254)
             :dirt (rgb 212 149 43)
             :pale-yellow (rgb 247 210 129)
             :brown (rgb 120 61 43)
             :night-sky (rgb 26 50 62)
             :angry-red (rgb 180 28 23)
             :hero-red (rgb 242 60 39)
             :near-black (rgb 4 4 4)
             :black (rgb 0 0 0)
             :near-white (rgb 250 250 250)
             :white (rgb 255 255 255)})

(defn color [key]
  (get colors key))

(defn ->hex [color]
  (as-hex color))

(defn ->rgb [hex]
  (hex->rgb hex))

(def invert garden.color/invert)
