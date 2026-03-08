(ns fable.print.lulu
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defonce ^:private token-cache (atom {:token nil :expires-at nil}))

(defn- lulu-cfg [] (:lulu env/config))
(defn- base-url [] (:base-url (lulu-cfg) "https://api.lulu.com"))

(defn- fetch-token []
  (let [{:keys [client-id client-secret]} (lulu-cfg)
        resp (http/post (str (base-url) "/auth/realms/glasstree/protocol/openid-connect/token")
                        {:form-params {:grant_type    "client_credentials"
                                       :client_id     client-id
                                       :client_secret client-secret}
                         :as :json})]
    (when (= 200 (:status resp))
      (let [body      (:body resp)
            token     (:access_token body)
            expires-in (:expires_in body 3600)
            expires-at (+ (System/currentTimeMillis) (* (- expires-in 60) 1000))]
        {:token token :expires-at expires-at}))))

(defn- get-token []
  (let [{:keys [token expires-at]} @token-cache]
    (if (and token (> expires-at (System/currentTimeMillis)))
      token
      (let [new-cache (fetch-token)]
        (reset! token-cache new-cache)
        (:token new-cache)))))

(defn- auth-headers []
  {"Authorization" (str "Bearer " (get-token))
   "Content-Type"  "application/json"})

(defn create-print-job
  "Create a Lulu print job. Returns the print job map."
  [{:keys [title quantity shipping-address]}]
  (let [body {:line_items [{:title           title
                            :quantity        quantity
                            :printable_normalization "COIL_BIND_IMAGE_WRAP"
                            :pod_package_id  "0600X0900BWSTDPB060UW444MXX"}]
              :shipping_address shipping-address
              :shipping_level  "MAIL"}
        resp (http/post (str (base-url) "/print-jobs/")
                        {:headers          (auth-headers)
                         :body             (json/generate-string body)
                         :as               :json
                         :throw-exceptions false})]
    (when (= 201 (:status resp))
      (:body resp))))

(defn upload-pdf
  "Upload interior PDF to a Lulu print job."
  [job-id pdf-bytes]
  (let [resp (http/post (str (base-url) "/print-jobs/" job-id "/interior/")
                        {:headers          {"Authorization" (str "Bearer " (get-token))}
                         :multipart        [{:name      "file"
                                             :content   pdf-bytes
                                             :filename  "interior.pdf"
                                             :mime-type "application/pdf"}]
                         :as               :json
                         :throw-exceptions false})]
    (:body resp)))

(defn get-job-status [job-id]
  (let [resp (http/get (str (base-url) "/print-jobs/" job-id "/")
                       {:headers          (auth-headers)
                        :as               :json
                        :throw-exceptions false})]
    (:body resp)))
