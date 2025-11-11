(ns rx-animation.color
  (:require [garden.color :refer [rgb as-hex]]))

(defn- color-range [value]
  (-> value
      (min 255.0)
      (max 0.0)
      (Math/floor)))

(defn tween [start-color end-color]
  (fn [t]
    (let [r-range (- (:red end-color) (:red start-color))
          g-range (- (:green end-color) (:green start-color))
          b-range (- (:blue end-color) (:blue start-color))]
      (as-hex (rgb (color-range (+ (:red start-color) (* r-range t)))
                   (color-range (+ (:green start-color) (* g-range t)))
                   (color-range (+ (:blue start-color) (* b-range t))))))))
