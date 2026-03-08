(ns fable.meta.client
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]))

(def ^:private graph-base "https://graph.facebook.com/v18.0")

(defn graph-get
  ([path access-token]
   (graph-get path access-token {}))
  ([path access-token extra-params]
   (let [resp (http/get (str graph-base path)
                        {:query-params (merge {:access_token access-token} extra-params)
                         :as           :json
                         :throw-exceptions false})]
     (if (= 200 (:status resp))
       (:body resp)
       (throw (ex-info "Meta Graph API error"
                       {:status (:status resp)
                        :body   (:body resp)}))))))
