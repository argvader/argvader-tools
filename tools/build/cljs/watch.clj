(ns build.cljs.watch
  (:require [shadow.cljs.devtools.api :as dapi]
            [shadow.cljs.devtools.server :as server]
            [build.cljs.shadow :as shadow]))

(defn -main [& args]
  (println "Watching cljs...")
  (server/start!)
  (shadow/watch))
