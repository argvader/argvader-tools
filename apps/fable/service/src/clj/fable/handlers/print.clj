(ns fable.handlers.print
  (:require
   [cheshire.core :as json]
   [fable.db.store :as store]
   [fable.storybook.pdf :as pdf]
   [fable.print.lulu :as lulu]))

(defn submit-handler [_db]
  (fn [request]
    (try
      (let [id        (get-in request [:path-params :id])
            body      (:json-params request)
            storybook (store/find-storybook id)]
        (if-not storybook
          {:status  404
           :headers {"Content-Type" "application/json"}
           :body    (json/generate-string {:error "Storybook not found"})}
          (let [theme     (keyword (:theme storybook))
                data      (json/parse-string (str (:data storybook)) true)
                pdf-bytes (pdf/generate-pdf data theme)
                job       (lulu/create-print-job
                           {:title            (:title data "My Fable")
                            :quantity         (or (:quantity body) 1)
                            :shipping-address (:shipping-address body)})]
            (when job
              (lulu/upload-pdf (:id job) pdf-bytes))
            {:status  201
             :headers {"Content-Type" "application/json"}
             :body    (json/generate-string {:lulu-job-id (:id job)
                                              :status      (:status job)})})))
      (catch Exception e
        {:status  500
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error (.getMessage e)})}))))

(defn status-handler [_db]
  (fn [request]
    (let [id         (get-in request [:path-params :id])
          lulu-job-id (get-in request [:query-params "lulu-job-id"])]
      (if-not lulu-job-id
        {:status  400
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error "Missing lulu-job-id"})}
        (let [status (lulu/get-job-status lulu-job-id)]
          {:status  200
           :headers {"Content-Type" "application/json"}
           :body    (json/generate-string status)})))))
