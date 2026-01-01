(ns challenge.day1b
  (:require [clojure.string :as str]))

(def starting-point "R50")
(def dial-size 100)

(defn left-right 
   [rotate]
   (let [dir (first rotate)
         amount (Integer/parseInt (subs rotate 1))]
     (if (= \L dir)
         (- amount)
         amount)))

(defn dial-mod [current rotation]
  (mod (+ current rotation dial-size) dial-size))    

(defn rotate-dial
  [rotations]
  (loop [current 0
         rotations-left rotations
         positions []
         passes 0]
    (if (empty? rotations-left)
       {:positions positions
        :pass-count passes}
       (let [rotation (left-right (first rotations-left))
             new-pos (dial-mod current rotation)
             wrap-pos? (or (and (> rotation 0) (<= new-pos current))
                           (and (< rotation 0) (>= new-pos current)))
             passed-zero? (or (= new-pos 0) wrap-pos?)]
         (recur new-pos (rest rotations-left) (conj positions new-pos)
                (if passed-zero? (inc passes) passes))))))

(defn return-passes [result]
  (println (:pass-count result)))
             
(defn -main
  [& args]
  (println "Finding Password")
  (-> (first args)
      (slurp)
      (str/split-lines)
      (seq)
      (conj starting-point)
      (rotate-dial)
      (return-passes)))
