(ns fable.handlers.pdf
  (:require
   [cheshire.core :as json]
   [fable.db.store :as store]
   [fable.storybook.pdf :as pdf]
   [amazonica.aws.s3 :as s3]
   [env.config :as env])
  (:import
   [java.io ByteArrayInputStream]))

(defn- s3-bucket [] (get-in env/config [:s3 :bucket]))
(defn- s3-region [] (get-in env/config [:s3 :region] "us-east-1"))

(defn- upload-to-s3 [key pdf-bytes]
  (s3/put-object :bucket-name (s3-bucket)
                 :key key
                 :input-stream (ByteArrayInputStream. pdf-bytes)
                 :metadata {:content-length (count pdf-bytes)
                            :content-type   "application/pdf"})
  key)

(defn- presigned-url [key]
  (let [expiry (java.util.Date. (+ (System/currentTimeMillis) (* 60 60 1000)))]
    (str (.toString
          (s3/generate-presigned-url :bucket-name (s3-bucket)
                                     :key key
                                     :expiration expiry)))))

(defn generate-handler [_db]
  (fn [request]
    (try
      (let [id        (get-in request [:path-params :id])
            storybook (store/find-storybook id)]
        (if-not storybook
          {:status  404
           :headers {"Content-Type" "application/json"}
           :body    (json/generate-string {:error "Storybook not found"})}
          (let [theme     (keyword (:theme storybook))
                data      (json/parse-string (str (:data storybook)) true)
                pdf-bytes (pdf/generate-pdf data theme)
                s3-key    (str "pdfs/" id ".pdf")]
            (upload-to-s3 s3-key pdf-bytes)
            (store/update-storybook-pdf! id s3-key)
            (let [url (presigned-url s3-key)]
              {:status  302
               :headers {"Location" url}
               :body    ""}))))
      (catch Exception e
        {:status  500
         :headers {"Content-Type" "application/json"}
         :body    (json/generate-string {:error (.getMessage e)})}))))
