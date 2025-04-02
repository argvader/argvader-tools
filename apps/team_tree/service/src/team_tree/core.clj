(ns team-tree.core
 (:require
    [aero.core :as aero]
    [clojure.java.io :as io]
    [env.config :as env]
    [env.core :as env-core]
    [com.walmartlabs.lacinia :as lacinia]
    [clojure.java.browse :refer [browse-url]]
    [team-tree.system :as system]
    [clojure.walk :as walk]
    [com.stuartsierra.component :as component])
 (:import (clojure.lang IPersistentMap)))

(defonce system (system/new-system))

(defn start
  [profile]
  (->
     profile
     (env-core/read-env)
     (env-core/set-env!))
  (alter-var-root #'system component/start-system)
  (if (= profile "dev")
    (browse-url "http://localhost:8888/"))
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

(defn -main [& args]
  (let [profile (first args)]
    (println "Starting server...")
    (println (str)"Profile: " profile)
    (start profile)))
