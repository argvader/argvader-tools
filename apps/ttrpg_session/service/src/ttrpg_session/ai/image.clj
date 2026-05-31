(ns ttrpg-session.ai.image
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defn- api-key [] (get-in env/config [:openai :api-key]))

(defn generate-scene-image
  "Call DALL-E 3 with prompt. Returns raw PNG bytes (byte[]).
   Uses b64_json response format to avoid a second HTTP round-trip."
  ^bytes [prompt]
  (let [body {:model           "dall-e-3"
              :prompt          prompt
              :n               1
              :size            "1792x1024"
              :response_format "b64_json"}
        resp (http/post "https://api.openai.com/v1/images/generations"
                        {:headers          {"Authorization" (str "Bearer " (api-key))
                                            "Content-Type"  "application/json"}
                         :body             (json/generate-string body)
                         :as               :json
                         :throw-exceptions false})]
    (if (= 200 (:status resp))
      (.decode (java.util.Base64/getDecoder)
               ^String (get-in resp [:body :data 0 :b64_json]))
      (throw (ex-info "DALL-E 3 error"
                      {:status (:status resp) :body (:body resp)})))))
