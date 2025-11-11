(ns challenge.day11.monkey-builder
  (:require [clojure.string :as str]))

(defn pop-last [args]
  (Integer/parseInt (last (str/split (nth args 1) #" "))))

(defmulti transform-monkey-part (fn [type _] (keyword type)))

(defmethod transform-monkey-part :Starting_items
  [_ args]
  (let [values (->> (str/split (nth args 1) #",")
                    (map #(Integer/parseInt (str/trim %))))]
    {:items (vec values)}))

(defmethod transform-monkey-part :Operation
  [_ args]
  (let [equation (str/split (str/trim (nth args 1)) #" ")]
    {:operation (str "(" (nth equation 3) " " (nth equation 2) " " (nth equation 4) ")")}))

(defmethod transform-monkey-part :Test
  [_ args]
  {:test (pop-last args)})

(defmethod transform-monkey-part :If_true
  [_ args]
  {:if-true (pop-last args)})

(defmethod transform-monkey-part :If_false
  [_ args]
  {:if-false (pop-last args)})

(defmethod transform-monkey-part :default
  [type _]
  (let [name (name type)]
    {:id (Integer/parseInt (nth (str/split name #"\_") 1))}))

(defn- transform-monkey [line]
  (let [parts (str/split line #":")
        key (str/replace (str/trim (first parts)) " " "_")]
    (transform-monkey-part key parts)))

(defn- monkey-builder [monkey-string]
  (let [monkey (str/split-lines monkey-string)]
    (into {:inspects 0} (map transform-monkey monkey))))

(defn- monkey-parser [data-list]
  (map monkey-builder data-list))

(defn- store-data! [data store]
   (reset! store data))

(defn build-monkey-data
  [file store]
  (-> file
      (slurp)
      (str/split #"(?im)^$\n")
      (monkey-parser)
      (store-data! store)))
