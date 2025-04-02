(ns build.serverless.deploy
  (:require [clojure.java.shell :refer [sh]]))

(defn -main [& args]
   (println "Starting Serverless...")
   (println (:out (sh "sls" "deploy"))))
