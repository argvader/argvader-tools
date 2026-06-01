(ns ttrpg-session.handlers.session
  (:require
   [cheshire.core :as json]
   [ttrpg-session.db.store :as store]
   [ttrpg-session.config.roster :as roster]
   [ttrpg-session.pipeline.processor :as processor]))

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
        (try
          (let [path (processor/republish-session! id)]
            {:status  200
             :headers {"Content-Type" "application/json"}
             :body    (json/generate-string {:published path})})
          (catch Exception e
            {:status  500
             :headers {"Content-Type" "application/json"}
             :body    (json/generate-string {:error (.getMessage e)})}))
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Session or summary not found"})}))))

(defn reload-roster-handler []
  (fn [_request]
    (roster/reload!)
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string {:status "ok"})}))
