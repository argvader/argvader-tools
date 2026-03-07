(ns challenge.day1a
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
         positions []]
    (if (empty? rotations-left)
       positions
       (let [rotation (left-right (first rotations-left))
             new-pos (dial-mod current rotation)]
         (recur new-pos (rest rotations-left) (conj positions new-pos))))))

(defn count-zero [positions]
  (->> positions
       (filter #(= % 0))
       (count)))
             
(defn -main
  [& args]
  (println "Finding Password")
  (-> (first args)
      (slurp)
      (str/split-lines)
      (seq)
      (conj starting-point)
      (rotate-dial)
      (count-zero)
      (println)))
