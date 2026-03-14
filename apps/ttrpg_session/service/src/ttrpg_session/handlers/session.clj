(ns ttrpg-session.handlers.session
  (:require
   [cheshire.core :as json]
   [ttrpg-session.db.store :as store]
   [ttrpg-session.config.roster :as roster]
   [ttrpg-session.site.generator :as generator]
   [ttrpg-session.site.publisher :as publisher]))

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
        (let [merged     (json/parse-string (json/generate-string (:transcript summary)) true)
              html       (generator/session-page session summary merged)
              s3-key     (publisher/publish-session! id html)
              _          (store/update-summary-published! id s3-key)
              idx-html   (generator/index-page (store/list-recording-sessions))
              _          (publisher/update-index! idx-html)]
          {:status  200
           :headers {"Content-Type" "application/json"}
           :body    (json/generate-string {:s3-key s3-key})})
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Session or summary not found"})}))))

(defn reload-roster-handler []
  (fn [_request]
    (roster/reload!)
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string {:status "ok"})}))
