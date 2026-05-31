(ns ttrpg-session.ai.narrator
  (:require
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [ttrpg-session.ai.client :as client]
   [ttrpg-session.model.session :as model]))

(def ^:private system-prompt
  "You are a skilled chronicler for tabletop RPG sessions.
Given a session transcript, produce a rich session record as a single JSON object.
All array fields may be empty []. Include all fields listed below.

Required fields:
- session_title: string — a dramatic in-world title for this session (2-5 words)
- overview: string — 2-4 paragraphs of flowing prose separated by \\n\\n
- key_events: array of {\"title\": string, \"body\": string} — 4-8 major story beats with a short title and 1-3 sentence body
- scene_title: string — title for the most visually dramatic or memorable moment
- scene_prose: string — 2-3 sentence vivid literary prose recreating that scene
- scene_image_prompt: string — DALL-E 3 prompt under 400 characters, no text in image, cinematic fantasy art style

Optional fields (include only when clearly present in the session):
- memorable_moments: array of strings — notable quotes or moments with context and attribution
- lore_learned: array of strings — world facts, history, or plot revelations discovered this session
- items: array of {\"item\": string, \"holder\": string, \"detail\": string} — items gained or noted
- mysteries: array of strings — open questions or unresolved threads raised this session
- commitments: array of strings — oaths, debts, alliances, or obligations incurred
- next_steps: array of strings — what the party intends to do next
- timeline: array of {\"time\": string, \"event\": string} — in-world chronological timeline
- npcs: array of {\"name\": string, \"slug\": string, \"description\": string, \"session_notes\": string}
         slug must be lowercase-hyphenated, e.g. \"elder-marcus\"
- locations: array of {\"name\": string, \"slug\": string, \"description\": string, \"session_notes\": string}
              slug must be lowercase-hyphenated, e.g. \"the-sunken-archive\"
- pc_moments: array of {\"character_name\": string, \"moment\": string} — one highlight per player character

Return only valid JSON with no additional commentary.")

(defn- ->clj
  "Convert snake_case GPT JSON response to kebab-case Clojure map."
  [raw]
  {:session-title      (:session_title raw)
   :overview           (:overview raw)
   :key-events-detail  (mapv (fn [e] {:title (:title e) :body (:body e)})
                              (:key_events raw []))
   :memorable-moments  (vec (:memorable_moments raw []))
   :lore-learned       (vec (:lore_learned raw []))
   :items              (mapv (fn [e] {:item   (:item e)
                                      :holder (:holder e)
                                      :detail (:detail e)})
                              (:items raw []))
   :mysteries          (vec (:mysteries raw []))
   :commitments        (vec (:commitments raw []))
   :next-steps         (vec (:next_steps raw []))
   :timeline           (mapv (fn [e] {:time (:time e) :event (:event e)})
                              (:timeline raw []))
   :scene-title        (:scene_title raw)
   :scene-prose        (:scene_prose raw)
   :scene-image-prompt (:scene_image_prompt raw)
   :npcs               (mapv (fn [e] {:name          (:name e)
                                      :slug          (:slug e)
                                      :description   (:description e)
                                      :session-notes (:session_notes e)})
                              (:npcs raw []))
   :locations          (mapv (fn [e] {:name          (:name e)
                                      :slug          (:slug e)
                                      :description   (:description e)
                                      :session-notes (:session_notes e)})
                              (:locations raw []))
   :pc-moments         (mapv (fn [e] {:character-name (:character_name e)
                                      :moment         (:moment e)})
                              (:pc_moments raw []))})

(defn summarize
  "Generate a rich wiki summary from a merged transcript.
   transcript is a seq of {:start float :character-name str :text str}.
   Returns a map conforming to ::model/gpt-rich-summary."
  [transcript campaign-name]
  (let [lines  (->> transcript
                    (map (fn [{:keys [character-name text]}]
                           (str character-name ": " text)))
                    (str/join "\n"))
        prompt (str "Campaign: " (or campaign-name "Unknown Campaign") "\n\n"
                    "Session transcript:\n" lines)
        resp   (client/chat-completion
                [{:role "system" :content system-prompt}
                 {:role "user"   :content prompt}]
                :json-mode? true
                :max-tokens 8192)
        raw    (json/parse-string (client/extract-text resp) true)
        result (->clj raw)]
    (when-not (s/valid? ::model/gpt-rich-summary result)
      (throw (ex-info "GPT summary failed spec validation"
                      {:raw     raw
                       :explain (s/explain-str ::model/gpt-rich-summary result)})))
    result))
