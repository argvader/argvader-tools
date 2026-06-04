(ns ttrpg-session.republish
  "CLI entry point to re-generate and re-publish a session's wiki to GitHub
   without re-running Discord capture or the AI pipeline. Reuses the stored
   summary/NPCs/locations.

   Usage: clj -M:republish <session-id> [profile]   (profile defaults to dev)"
  (:require
   [com.stuartsierra.component :as component]
   [env.core :as env-core]
   [ttrpg-session.db.store :as store]
   [ttrpg-session.pipeline.processor :as processor])
  (:gen-class))

(defn -main [& [session-id profile]]
  (when (nil? session-id)
    (println "Usage: clj -M:republish <session-id> [profile]  (profile defaults to dev)")
    (System/exit 1))
  (-> (or profile "dev") env-core/read-env env-core/set-env!)
  (let [db (component/start (store/map->ScribeDB {}))]
    (try
      (let [path (processor/republish-session! session-id)]
        (println "Republished" session-id "->" path))
      (finally
        (component/stop db))))
  (shutdown-agents))
