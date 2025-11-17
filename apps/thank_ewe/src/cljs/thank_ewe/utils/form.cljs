(ns thank-ewe.utils.form
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [goog.string :as gstring]))

;; form-decode from ring.util.codec
(defn- assoc-conj
  "Associate a key with a value in a map. If the key already exists in the map,
  a vector of values is associated with the key."
  [m k v]
  (assoc m k
         (if-let [cur (get m k)]
           (if (vector? cur)
             (conj cur v)
             [cur v])
           v)))

(defn- form-decode-str
  "Decode the supplied www-form-urlencoded string."
  [^String encoded]
  (gstring/urlDecode encoded))

(defn- decode-data
  "Decode the supplied www-form-urlencoded string. Returns either a string
  or a keywordized map of parameters."
  [^String encoded]
  (if-not (gstring/contains encoded "=")
    (form-decode-str encoded)
    (reduce
      (fn [m param]
        (if-let [[k v] (str/split param #"=" 2)]
          (assoc-conj m (form-decode-str k) (form-decode-str v))
          m))
      {}
      (str/split encoded #"&"))))

(defn decode [data]
  (when (some? data)
    (-> data decode-data keywordize-keys)))

