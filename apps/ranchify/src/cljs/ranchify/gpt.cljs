(ns ranchify.gpt
  (:require-macros [adzerk.env :as env])
  (:require ["openai" :as openai]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(env/def OPENAI_KEY :required)

(def model "text-davinci-003")
(def rnd_key "amxbranchify")
(def prompt "Rewrite the following statement within a random key in the style of a colliquial western cowboy ")

(def configuration (new openai/Configuration (js-obj "apiKey" OPENAI_KEY)))
(def openai (new openai/OpenAIApi configuration))

(defn translate-cowboy [text]
  (go
    (let [params (js-obj "model" model "prompt" (str prompt " " rnd_key text rnd_key))
          completion (<p! (.createCompletion openai params))]
      (print completion)
      (aget completion "data" "choices" 0 "text"))))


