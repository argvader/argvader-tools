(ns code-churn.core
 (:require
    [env.core :as env-core]
    [code-churn.system :as system]
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
  :started)

(defn stop
  []
  (alter-var-root #'system component/stop-system)
  :stopped)

(defn -main [& args]
  (let [profile (first args)]
    (println "Starting churn server...")
    (println (str)"Profile: " profile)
    (start profile)))
