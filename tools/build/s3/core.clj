(ns build.s3.core
  (:require [clojure.string                       :as str]
            [amazonica.aws.s3                     :as s3]
            [amazonica.aws.cloudfront             :as cf]
            [shadow.cljs.devtools.server.npm-deps :as npm]
            [build.aws                            :refer [set-profile!]]))

(def base-dir "./resources/public/")

(defn args?
  [args] (when (seq args) args))

(defn- production-file? [file]
  (let [path (.getPath file)]
    (and (.isFile file)
      (not (or
            (str/includes? path ".DS_Store")
            (str/includes? path "cljs-runtime"))))))

(defn- deploy [file bucket]
  (s3/put-object bucket
                 (str/replace (.getPath file) base-dir "")
                 file))

(defn -main [& args]
   (let [package-json (npm/read-package-json nil)
         name (get package-json "name")]
      (set-profile!)
      (doseq [file (file-seq (clojure.java.io/file base-dir))]
        (if (production-file? file)
          (deploy file (str "argvader-" name))))))
