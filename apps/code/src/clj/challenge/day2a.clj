(ns challenge.day2a
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(def rps-scoring {:r 1 :p 2 :s 3})
(def round-scoring {:win 6 :draw 3 :lose 0})
(def key-map {"A" :r "B" :p "C" :s "X" :r "Y" :p "Z" :s})

(defn- round-result
  [elf-choice my-choice]
  (cond
    (= my-choice elf-choice) :draw
    (or (and (= my-choice :r) (= elf-choice :s))
        (and (= my-choice :p) (= elf-choice :r))
        (and (= my-choice :s) (= elf-choice :p))) :win
    :else :lose))

(defn- score-round
  [[choice result]]
  (+ (choice rps-scoring) (result round-scoring)))

(defn- parse-data 
  [[elf-data my-data]]
  (let [my-choice (get key-map my-data)
        elf-choice (get key-map elf-data)
        result (round-result elf-choice my-choice)]
    [my-choice result]))

(defn -main
  [& args]
  (println "Scoring Rock Paper Scissors Total: "
    (->> (first args)
         (slurp)
         (str/split-lines)
         (map #(str/split % #" "))
         (map parse-data)
         (map score-round)
         (reduce +))))
