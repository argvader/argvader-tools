(ns fable.handlers.media
  (:require
   [cheshire.core :as json]
   [fable.auth.session :as session]
   [fable.meta.posts :as posts]))

(defn fetch-handler []
  (fn [request]
    (try
      (let [sess        (:session request)
            meta-token  (session/get-meta-token sess)
            all-posts   (posts/fetch-all-posts meta-token)]
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:posts all-posts})})
      (catch Exception e
        {:status  500
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error (.getMessage e)})}))))
