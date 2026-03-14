(ns ttrpg-session.model.session
  (:require [clojure.spec.alpha :as s]))

;; recording-session
(s/def ::id string?)
(s/def ::discord-guild string?)
(s/def ::channel-name string?)
(s/def ::campaign-name (s/nilable string?))
(s/def ::status #{"recording" "processing" "done" "error"})
(s/def ::started-at inst?)
(s/def ::ended-at (s/nilable inst?))
(s/def ::error-msg (s/nilable string?))

(s/def ::recording-session
  (s/keys :req-un [::id ::discord-guild ::channel-name ::status ::started-at]
          :opt-un [::campaign-name ::ended-at ::error-msg]))

;; speaker-track
(s/def ::discord-user-id string?)
(s/def ::discord-username string?)
(s/def ::character-name (s/nilable string?))
(s/def ::transcript (s/nilable map?))

(s/def ::speaker-track
  (s/keys :req-un [::id ::discord-user-id ::discord-username]
          :opt-un [::character-name ::transcript]))

;; session-summary / GPT output
(s/def ::narrative string?)
(s/def ::key-events (s/coll-of string?))
(s/def ::character string?)
(s/def ::quote string?)
(s/def ::notable-quote (s/keys :req-un [::character ::quote]))
(s/def ::notable-quotes (s/coll-of ::notable-quote))
(s/def ::site-s3-key (s/nilable string?))

(s/def ::session-summary
  (s/keys :req-un [::id ::narrative ::key-events ::notable-quotes]
          :opt-un [::site-s3-key]))

(s/def ::gpt-summary
  (s/keys :req-un [::narrative ::key-events ::notable-quotes]))
