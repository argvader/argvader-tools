(ns acronym.loader
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [amazonica.core :refer [defcredential]]
            [amazonica.aws.dynamodbv2 :as db]))

(def creds {:profile "asurion-soluto-prod.appadmins"
            :endpoint "us-east-1"})

(def table-name "asurion_acronyms")

(defn- strip-font-tags 
  [value]
  (-> value
      (str/replace #"<font>" "")
      (str/replace #"<\/font>" "")))
  

(defn- empty-string?
  [data]
  (let [key (nth data 0)]
    (if (and (string? key) (empty? key))
      false
      true)))

(defn- single-word?
  [data]
  (let [key (nth data 0)]
    (= (count (str/split key #" ")) 1)))

(defn- find-item [key]
  (db/get-item
   creds
   :table-name table-name
   :key {:acronym {:s (str/upper-case key)}}))

(defn- insert!
  [key value]
  (db/put-item
   creds
   :table-name table-name
   :item {:acronym (str/upper-case key)
          :definition #{(strip-font-tags value)}}))

(defn- write-data!
  [data]
  (let [key (nth data 0)
        value (nth data 1)]
    (if (empty? (find-item key))
      (insert! key value)
      (println (str "Found: " key)))
    data))

(defn- read-csv
  [file-path]
  (with-open [reader (io/reader file-path)]
    (->> (csv/read-csv reader)
         (rest)
         (filter empty-string?)
         (filter single-word?)
         (map write-data!)
         (dorun))))

(defn -main
  [& args]
  (println "Loading CSV Data!!!!!")
  (defcredential (:creds creds))
  (read-csv (first args)))
