(ns ttrpg-session.model.session
  (:require [clojure.spec.alpha :as s]))

;; --- recording-session ---

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

;; --- speaker-track ---

(s/def ::discord-user-id string?)
(s/def ::discord-username string?)
(s/def ::character-name (s/nilable string?))
(s/def ::transcript (s/nilable map?))

(s/def ::speaker-track
  (s/keys :req-un [::id ::discord-user-id ::discord-username]
          :opt-un [::character-name ::transcript]))

;; --- rich GPT summary ---

(s/def ::session-title string?)
(s/def ::overview string?)
(s/def ::title string?)
(s/def ::body string?)
(s/def ::slug string?)
(s/def ::description string?)
(s/def ::session-notes string?)
(s/def ::holder string?)
(s/def ::detail string?)
(s/def ::item string?)
(s/def ::time string?)
(s/def ::event string?)
(s/def ::moment string?)
(s/def ::scene-title string?)
(s/def ::scene-prose string?)
(s/def ::scene-image-prompt string?)

(s/def ::key-event-detail   (s/keys :req-un [::title ::body]))
(s/def ::item-entry         (s/keys :req-un [::item ::holder ::detail]))
(s/def ::timeline-entry     (s/keys :req-un [::time ::event]))
(s/def ::npc-entry          (s/keys :req-un [::name ::slug ::description ::session-notes]))
(s/def ::location-entry     (s/keys :req-un [::name ::slug ::description ::session-notes]))
(s/def ::pc-moment-entry    (s/keys :req-un [::character-name ::moment]))

(s/def ::key-events-detail  (s/coll-of ::key-event-detail))
(s/def ::memorable-moments  (s/coll-of string?))
(s/def ::lore-learned       (s/coll-of string?))
(s/def ::items              (s/coll-of ::item-entry))
(s/def ::mysteries          (s/coll-of string?))
(s/def ::commitments        (s/coll-of string?))
(s/def ::next-steps         (s/coll-of string?))
(s/def ::timeline           (s/coll-of ::timeline-entry))
(s/def ::npcs               (s/coll-of ::npc-entry))
(s/def ::locations          (s/coll-of ::location-entry))
(s/def ::pc-moments         (s/coll-of ::pc-moment-entry))

(s/def ::gpt-rich-summary
  (s/keys :req-un [::session-title
                   ::overview
                   ::key-events-detail
                   ::scene-title
                   ::scene-prose
                   ::scene-image-prompt]
          :opt-un [::memorable-moments
                   ::lore-learned
                   ::items
                   ::mysteries
                   ::commitments
                   ::next-steps
                   ::timeline
                   ::npcs
                   ::locations
                   ::pc-moments]))

;; legacy spec retained for any existing callers
(s/def ::narrative string?)
(s/def ::key-events (s/coll-of string?))
(s/def ::character string?)
(s/def ::quote string?)
(s/def ::notable-quote (s/keys :req-un [::character ::quote]))
(s/def ::notable-quotes (s/coll-of ::notable-quote))
(s/def ::site-s3-key (s/nilable string?))

(s/def ::gpt-summary
  (s/keys :req-un [::narrative ::key-events ::notable-quotes]))
