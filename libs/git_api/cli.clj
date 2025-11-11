(ns git-api.cli
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]))

(def config (atom {}))

(defn- bash
  ([dir command]
   (sh "bash" "-c" command :dir dir))
  ([command]
   (sh "bash" "-c" command)))

(defn- expand [path]
  (-> (str "echo -n " path)
      bash
      :out))

(defn- tail-recursive-delete
  [& fs]
  (when-let [f (first fs)]
    (if-let [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f true)
          (recur (rest fs))))))

(defn init [data]
  (println "git cli [initializing]")
  (reset! config (merge data {:build-dir (expand (:build-dir data))})))

(defn close []
  (println "git cli [stopping]"))

(defn clean [directory]
  (tail-recursive-delete directory))

(defn build-log [name after]
  (println (:build-dir @config))
  (let [dir (:build-dir @config)
        exec (partial bash dir)]
    (exec (str "git log --all --numstat --date=short --pretty=format:'--%h--%ad--%aN' --no-renames --after=" after " > " name ".log"))))

(defn clone [repo]
  (bash (str "git clone " repo " " (:build-dir @config))))

(defn trial [git-path]
  (let [project (get (re-find #"\/(.*)\.git" git-path) 1)]
    (println project)
    (clean (:build-dir @config))
    (clone git-path)
    (build-log project "2018-01-01")))
