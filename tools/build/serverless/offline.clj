(ns build.serverless.offline
  (:require [clojure.java.shell :refer [sh]]))

(defn flatten-code []
  (js/console.warn "flatten called"))

(defn -main [& args]
   (println "Serverless offline...")
   (println (:out (sh "sls" "offline" "start"))))
