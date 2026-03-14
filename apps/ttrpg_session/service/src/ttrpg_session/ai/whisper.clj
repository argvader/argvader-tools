(ns ttrpg-session.ai.whisper
  (:require
   [clj-http.client :as http]
   [env.config :as env]))

(defn- api-key []       (get-in env/config [:openai :api-key]))
(defn- whisper-model [] (get-in env/config [:openai :whisper-model] "whisper-1"))

(defn transcribe
  "Transcribe a WAV file via OpenAI Whisper.
   Returns the verbose_json response body (with :segments containing :start/:end/:text)."
  [^java.io.File wav-file]
  (let [resp (http/post "https://api.openai.com/v1/audio/transcriptions"
                        {:headers          {"Authorization" (str "Bearer " (api-key))}
                         :multipart        [{:name "file"                       :content wav-file}
                                            {:name "model"                      :content (whisper-model)}
                                            {:name "response_format"            :content "verbose_json"}
                                            {:name "timestamp_granularities[]"  :content "segment"}]
                         :as               :json
                         :throw-exceptions false})]
    (if (= 200 (:status resp))
      (:body resp)
      (throw (ex-info "Whisper API error"
                      {:status (:status resp) :body (:body resp)})))))
