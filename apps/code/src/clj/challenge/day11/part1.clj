(ns challenge.day11.part1
  (:require [clojure.string :as str]
            [challenge.day11.monkey-builder :as builder]))

(def monkey-data (atom ()))
(def rounds 20)

(defn echo [value]
  (println (str "echo: " value))
  value)

(defn- divisible?
  [div num]
  (zero? (mod num div)))

(defn- is-monkey? [old id]
  (= (:id old) id))

(defn- replace-monkey! [monkey]
  (let [this-monkey? (fn [old new] (if (= (:id old) (:id new)) new old))]
     (let [new-list (map #(this-monkey? % monkey) @monkey-data)]
       (reset! monkey-data new-list))))

(defn- throw-item [value id]
  (let [target-monkey (first (filter #(is-monkey? % id)  @monkey-data))]
     (replace-monkey! (assoc target-monkey :items (conj (:items target-monkey) value)))))

(defn- worry-level [monkey item]
    (let [formula (str/replace (:operation monkey) #"old" (str item)) 
          worry (eval (read-string formula))
          bored (long (Math/floor (/ worry 3)))
          condition (:test monkey)]
      (if (divisible? condition bored)
        (throw-item bored (:if-true monkey))
        (throw-item bored (:if-false monkey))))
    monkey)

(defn- inspect [monkey]
  (let [inspects (:inspects monkey)]
    (doseq [item (:items monkey)]
        (-> monkey
            (assoc :inspects (+ inspects (count (:items monkey))))
            (worry-level item)
            (assoc :items [])
            (replace-monkey!)))))
          

(defn process []
  (let [sorted (sort #(< (:id %1 ) (:id %2)) @monkey-data)]
    (doseq [monkey sorted]
      (let [current-monkey (first (filter #(is-monkey? % (:id monkey)) @monkey-data))]
        (inspect current-monkey)))))
     

(defn report []
  (let [sorted (sort #(> (:inspects %1 ) (:inspects %2)) @monkey-data)
        top (take 2 sorted)]
    (doseq [monkey sorted]
       (println "Monkey: " (:id monkey) " items: " (:items monkey)))
    (println (str "Top 2 inspects " (* (:inspects (nth top 0)) (:inspects (nth top 1)))))))

(defn -main
  [& args]
  (println "Monkey Business")
  (builder/build-monkey-data (first args) monkey-data)
  (dorun (- rounds 1) (repeatedly process))
  (report))
