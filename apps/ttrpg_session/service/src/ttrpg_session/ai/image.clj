(ns ttrpg-session.ai.image
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defn- api-key [] (get-in env/config [:openai :api-key]))

(defn- decode-image
  "Return PNG bytes from an images-API data entry. dall-e-3 defaults to a hosted
   URL (the `response_format` param was removed by OpenAI); other models return
   base64 directly. Handle whichever is present."
  ^bytes [entry]
  (if-let [b64 (:b64_json entry)]
    (.decode (java.util.Base64/getDecoder) ^String b64)
    (:body (http/get (:url entry) {:as :byte-array}))))

(defn generate-scene-image
  "Generate a scene image via OpenAI's image API. Returns raw PNG bytes (byte[]).
   Uses gpt-image-1 (dall-e-3 was retired); 1536x1024 is its landscape size.
   gpt-image-1 requires a verified OpenAI org and always returns base64."
  ^bytes [prompt]
  (let [body {:model   "gpt-image-1"
              :prompt  prompt
              :n       1
              :size    "1536x1024"
              :quality "medium"}
        resp (http/post "https://api.openai.com/v1/images/generations"
                        {:headers          {"Authorization" (str "Bearer " (api-key))
                                            "Content-Type"  "application/json"}
                         :body             (json/generate-string body)
                         :as               :json
                         :throw-exceptions false})]
    (if (= 200 (:status resp))
      (decode-image (get-in resp [:body :data 0]))
      (throw (ex-info (str "DALL-E image error (HTTP " (:status resp) "): "
                           (get-in resp [:body :error :message]))
                      {:status (:status resp) :body (:body resp)})))))
