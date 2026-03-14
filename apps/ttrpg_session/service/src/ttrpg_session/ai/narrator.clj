(ns ttrpg-session.ai.narrator
  (:require
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [ttrpg-session.ai.client :as client]
   [ttrpg-session.model.session :as model]))

(def ^:private system-prompt
  "You are a skilled chronicler for tabletop RPG sessions. Given a session transcript,
produce a summary in JSON with these exact fields:
- narrative: flowing prose summary of what happened (2-4 paragraphs, separated by \\n\\n)
- key_events: array of strings, each a concise bullet describing a major story beat
- notable_quotes: array of objects each with 'character' and 'quote' string fields
Return only valid JSON with no additional commentary.")

(defn summarize
  "Generate a narrative summary from a merged transcript.
   transcript is a seq of {:start float :character-name str :text str}.
   Returns {:narrative str :key-events [str] :notable-quotes [{:character :quote}]}."
  [transcript campaign-name]
  (let [lines   (->> transcript
                     (map (fn [{:keys [character-name text]}]
                            (str character-name ": " text)))
                     (str/join "\n"))
        prompt  (str "Campaign: " (or campaign-name "Unknown Campaign") "\n\n"
                     "Session transcript:\n" lines)
        resp    (client/chat-completion
                 [{:role "system" :content system-prompt}
                  {:role "user"   :content prompt}]
                 :json-mode? true)
        raw     (json/parse-string (client/extract-text resp) true)
        result  {:narrative      (:narrative raw)
                 :key-events     (vec (:key_events raw))
                 :notable-quotes (mapv #(hash-map :character (:character %)
                                                  :quote     (:quote %))
                                       (:notable_quotes raw))}]
    (when-not (s/valid? ::model/gpt-summary result)
      (throw (ex-info "GPT summary failed spec validation"
                      {:raw     raw
                       :explain (s/explain-str ::model/gpt-summary result)})))
    result))
