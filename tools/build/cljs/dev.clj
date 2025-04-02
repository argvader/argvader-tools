(ns build.cljs.dev
  (:require [shadow.cljs.devtools.api :as dapi]
            [shadow.cljs.devtools.server :as server]
            [build.cljs.shadow :as shadow]
            [dirac.agent :as dagent]))

(defn -main [& args]
  (println "Starting cljs...")
  (server/start!)
  (dapi/nrepl-select :browser)
  (shadow/watch "dev")
  (dagent/boot!))
