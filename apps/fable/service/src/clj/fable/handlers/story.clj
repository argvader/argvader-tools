(ns fable.handlers.story
  (:require
   [cheshire.core :as json]
   [fable.db.store :as store]
   [fable.auth.session :as session]
   [fable.storybook.builder :as builder]))

(defn generate-handler [_db]
  (fn [request]
    (try
      (let [sess       (:session request)
            session-id (str (:id sess))
            body       (:json-params request)
            posts      (:posts body)
            theme      (keyword (:theme body))
            storybook  (store/create-storybook! {:session-id session-id
                                                  :theme      theme
                                                  :data       {}})
            sb-id      (str (:id storybook))
            job        (store/create-job! sb-id)]
        ;; Run generation asynchronously
        (future
          (try
            (store/update-job! (str (:id job)) {:status "running" :progress 5 :error-msg nil})
            (let [result (builder/build-storybook
                          posts theme
                          (fn [pct]
                            (store/update-job! (str (:id job))
                                               {:status   "running"
                                                :progress (int pct)
                                                :error-msg nil})))]
              (store/create-storybook! {:session-id session-id :theme theme :data result})
              (store/update-job! (str (:id job)) {:status "done" :progress 100 :error-msg nil}))
            (catch Exception e
              (store/update-job! (str (:id job))
                                 {:status   "error"
                                  :progress 0
                                  :error-msg (.getMessage e)}))))
        {:status  202
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:job-id (str (:id job))
                                         :storybook-id sb-id})})
      (catch Exception e
        {:status  500
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error (.getMessage e)})}))))

(defn job-handler [_db]
  (fn [request]
    (let [job-id (get-in request [:path-params :job-id])
          job    (store/find-job job-id)]
      (if job
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:status       (:status job)
                                          :progress     (:progress job)
                                          :storybook-id (str (:storybook_id job))})}
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Job not found"})}))))

(defn get-handler [_db]
  (fn [request]
    (let [id        (get-in request [:path-params :id])
          storybook (store/find-storybook id)]
      (if storybook
        {:status  200
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string storybook)}
        {:status  404
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Storybook not found"})}))))
