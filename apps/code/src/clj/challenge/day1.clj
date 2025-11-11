(ns challenge.day1
    (:require [clojure.string :as str]))

(defn- group-elven-data
   [data]
   (->> data
        (partition-by #(= "" %))
        (filter #(not (and (= (count %) 1) (empty? (nth % 0)))))))

(defn- convert-numbers
  [string-sequence]
  (map #(Integer/parseInt %) string-sequence))

(defn- sum-calories
  [calories]
  (reduce + calories))

(defn -main
  [& args]
  (println "Finding Top Elf")
  (->> (first args)
       (slurp)
       (str/split-lines)
       (group-elven-data)
       (map convert-numbers)
       (map sum-calories)
       (sort >) 
       (take 3)
       (reduce +) 
       (println)))
