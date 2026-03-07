(ns challenge.2025.day1
  (:require [clojure.string :as str]))

(def starting-point 50)
(def dial-size 99)

(defn- turn-dial
 [command]
 (println (rest command)) 
 (if (=  \L (first command))
     (println "L")
     (println "R"))
 (Integer/parseInt (.s ^clojure.lang.StringSeq (rest command))))

(defn- calculate-passcode 
  [lines]
  (loop [remaining lines 
         total   starting-point] 
    (if (empty? remaining)
      total 
      (recur (rest remaining) 
             (+ total (turn-dial (first remaining)))))))

(defn -main
 [& args]
 (println "Finding Passcode")
 (->> (first args)       
      (slurp)
      (str/split-lines)
      (calculate-passcode)))

