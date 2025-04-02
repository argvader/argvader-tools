(ns checkmarx.core
  (:require [clj-pdf.core :as pdf]
            [yaml.core :as yaml]
            [clojure.java.io :as io])
  (:gen-class))

(def title-name "title.pdf")

(defn- clean-up [arg]
  (println "Cleaning Up")
  (io/delete-file title-name))

(defn- generate-title-page [config]
  (let [title (str (:partner config) " " (:title config))]
    (pdf/pdf [{}
              [:paragraph {:style :bold :size 36} title]
              [:paragraph {:style :bold :size 36} (str "Release: " (:version config))]]
             title-name)))

(defn- load-config [config]
  (println (str "Loading Config: " config))
  (yaml/from-file config))

(defn- interpose-title [files]
  (conj (interpose title-name files) title-name))

(defn- is-pdf? [file]
  (and (.isFile file) (clojure.string/includes? (.getName file) ".pdf")))

(defn- load-files [path]
  (let [directory (clojure.java.io/file path)
        files (filter is-pdf? (file-seq directory))]
    (for [file files] (str path "/" (.getName file)))))

(defn- merge-files [out-file files]
  (println "Building Checkmarx Report...")
  (apply pdf/collate out-file files))

(defn -main [& args]
  (let [config (load-config (first args))
        out-file (clojure.string/join "-" [(:title config) (:platform config) (:version config)])]
    (generate-title-page config)
    (->> (:files config)
         (load-files)
         (interpose-title)
         (merge-files (str out-file ".pdf"))
         (clean-up))))
