(ns ttrpg-session.core
  (:require
   [com.stuartsierra.component :as component]
   [env.core :as env-core]
   [ttrpg-session.system :as system])
  (:gen-class))

(defonce system (atom nil))

(defn start [profile]
  (-> profile env-core/read-env env-core/set-env!)
  (let [sys (component/start-system (system/new-system))]
    (reset! system sys)
    :started))

(defn stop []
  (when @system
    (component/stop-system @system)
    (reset! system nil))
  :stopped)

(defn -main [& args]
  (let [profile (or (first args) "dev")]
    (println "Starting ttrpg-session with profile:" profile)
    (start profile)))
