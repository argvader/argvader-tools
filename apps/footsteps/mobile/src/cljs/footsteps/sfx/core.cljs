(ns footsteps.sfx.core
  (:require [footsteps.sfx.library :as library]
            ["expo-av" :refer [Audio]]))

(defmulti sound (fn [type count] type))

(defmethod sound :heels [type count]
  (cond
    (even? count) library/heels-left
    :else library/heels-right))

(defmethod sound :spurs [type count]
  (cond
    (zero? (mod count 4)) library/spurs4
    (zero? (mod count 3)) library/spurs3
    (zero? (mod count 2)) library/spurs2
    :else library/spurs1))


(defn play-sound [active-sfx step-count]
  (let [player (.. Audio -Sound -createAsync)
        sound (sound active-sfx step-count)]
    (player sound (js-obj "shouldPlay" true))))
