(ns build.cljs.minified
  (:require [clojure.java.io :as io]
            [build.cljs.shadow :as shadow]))

(defn tail-recursive-delete
  [& fs]
  (when-let [f (first fs)]
    (if-let [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f)
          (recur (rest fs))))))

(defn -main [& args]
  (println "Release Build cljs...")
  (let [codepath (nth args 1 "resources/public/js")]
    (if (.isDirectory (io/file codepath))
       (tail-recursive-delete codepath))
    (shadow/release (first args))))
