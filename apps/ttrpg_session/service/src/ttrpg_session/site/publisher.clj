(ns ttrpg-session.site.publisher
  (:require
   [amazonica.aws.s3 :as s3]
   [amazonica.aws.cloudfront :as cf]
   [clojure.java.io :as io]
   [env.config :as env]))

(defn- bucket [] (get-in env/config [:s3 :bucket]))
(defn- cf-dist [] (get-in env/config [:s3 :cloudfront-dist-id]))

(defn- put-html!
  "Write content string as a UTF-8 HTML object to S3."
  [s3-key content]
  (let [tmp (java.io.File/createTempFile "ttrpg-" ".html")]
    (try
      (spit tmp content :encoding "UTF-8")
      (s3/put-object :bucket-name (bucket)
                     :key         s3-key
                     :file        tmp
                     :metadata    {:content-type "text/html; charset=utf-8"})
      (finally
        (.delete tmp)))))

(defn- invalidate! []
  (cf/create-invalidation
   :distribution-id (cf-dist)
   :invalidation-batch {:caller-reference (str (java.util.UUID/randomUUID))
                        :paths {:items ["/*"] :quantity 1}}))

(defn publish-session!
  "Upload session HTML to S3. Returns the S3 key."
  [session-id html]
  (let [key (str "sessions/" session-id "/index.html")]
    (put-html! key html)
    key))

(defn update-index!
  "Upload the index page and invalidate CloudFront."
  [index-html]
  (put-html! "index.html" index-html)
  (invalidate!))
