(ns fable.ai.client
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defn- api-key []  (get-in env/config [:openai :api-key]))
(defn- model []    (get-in env/config [:openai :model] "gpt-4o"))
(defn- max-tok []  (get-in env/config [:openai :max-tokens] 4096))

(defn chat-completion
  "Send a chat completion request to OpenAI. Returns the parsed response body."
  [messages & {:keys [json-mode?] :or {json-mode? false}}]
  (let [body (cond-> {:model       (model)
                      :max_tokens  (max-tok)
                      :messages    messages}
               json-mode? (assoc :response_format {:type "json_object"}))
        resp (http/post "https://api.openai.com/v1/chat/completions"
                        {:headers          {"Authorization" (str "Bearer " (api-key))
                                            "Content-Type"  "application/json"}
                         :body             (json/generate-string body)
                         :as               :json
                         :throw-exceptions false})]
    (if (= 200 (:status resp))
      (:body resp)
      (throw (ex-info "OpenAI API error"
                      {:status (:status resp)
                       :body   (:body resp)})))))

(defn extract-text [response]
  (get-in response [:choices 0 :message :content]))
