(ns ttrpg-session.handlers.session
  (:require
   [cheshire.core :as json]
   [ttrpg-session.db.store :as store]
   [ttrpg-session.config.roster :as roster]))

(defn list-handler [_db]
  (fn [_request]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string (store/list-recording-sessions))}))

(defn get-handler [_db]
  (fn [request]
    (let [id      (get-in request [:path-params :id])
          session (store/find-recording-session id)
          summary (when session (store/find-summary id))]
      (if session
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:session session :summary summary})}
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Session not found"})}))))

(defn publish-handler [_db]
  (fn [request]
    (let [id      (get-in request [:path-params :id])
          session (store/find-recording-session id)
          summary (when session (store/find-summary id))]
      (if (and session summary)
        ;; TODO: re-publishing must mirror steps 7-16 of pipeline.processor/process-session!
        ;; (rebuild the full mkdocs wiki from DB state and publisher/publish-files!).
        ;; The old S3 single-page republish was removed in the GitHub Pages refactor.
        {:status  501
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string
                   {:error "Re-publish is not yet implemented for the GitHub Pages pipeline."})}
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Session or summary not found"})}))))

(defn reload-roster-handler []
  (fn [_request]
    (roster/reload!)
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string {:status "ok"})}))
