(ns ttrpg-session.pipeline.processor
  (:require
   [ttrpg-session.db.store :as store]
   [ttrpg-session.audio.decoder :as decoder]
   [ttrpg-session.audio.writer :as writer]
   [ttrpg-session.ai.whisper :as whisper]
   [ttrpg-session.pipeline.merger :as merger]
   [ttrpg-session.ai.narrator :as narrator]
   [ttrpg-session.config.roster :as roster]
   [ttrpg-session.site.generator :as generator]
   [ttrpg-session.site.publisher :as publisher]))

(defn- transcribe-user!
  "Decode, downsample, write WAV, transcribe via Whisper, persist track row.
   Returns {:discord-username :character-name :transcript} for merging."
  [session-id user-id username pcm-bufs tmp-dir]
  (println (str "Transcribing user: " username))
  (let [raw-pcm    (decoder/concat-buffers pcm-bufs)
        mono-pcm   (decoder/downsample raw-pcm)
        wav        (writer/wav-file tmp-dir user-id)
        _          (writer/write-wav! mono-pcm wav)
        char-name  (roster/character-name username)
        track      (store/create-speaker-track!
                    {:session-id       session-id
                     :discord-user-id  user-id
                     :discord-username username
                     :character-name   char-name})
        transcript (whisper/transcribe wav)]
    (store/update-track-transcript! (str (:id track)) transcript)
    {:discord-username username
     :character-name   char-name
     :transcript       transcript}))

(defn process-session!
  "Orchestrate the full decode→transcribe→merge→summarize→publish pipeline.
   pcm-buffers: {user-id -> {:username str :pcm-bufs [byte[]]}}
   Runs inside a future launched from discord/commands."
  [session-id pcm-buffers _db]
  (try
    (println (str "Processing session: " session-id))
    (let [session  (store/find-recording-session session-id)
          tmp-dir  (writer/tmp-dir! session-id)
          tracks   (doall
                    (for [[user-id {:keys [username pcm-bufs]}] pcm-buffers
                          :when (seq pcm-bufs)]
                      (transcribe-user! session-id user-id username pcm-bufs tmp-dir)))
          merged   (merger/merge-transcripts tracks)
          summary  (narrator/summarize merged (:campaign_name session))
          _        (store/create-summary!
                    {:session-id     session-id
                     :transcript     merged
                     :narrative      (:narrative summary)
                     :key-events     (:key-events summary)
                     :notable-quotes (:notable-quotes summary)})
          html     (generator/session-page session summary merged)
          s3-key   (publisher/publish-session! session-id html)
          _        (store/update-summary-published! session-id s3-key)
          sessions (store/list-recording-sessions)
          idx-html (generator/index-page sessions)
          _        (publisher/update-index! idx-html)
          _        (store/complete-recording-session! session-id)]
      (println (str "Session " session-id " complete. Published: " s3-key)))
    (catch Exception e
      (println (str "Error processing session " session-id ": " (.getMessage e)))
      (store/fail-recording-session! session-id (.getMessage e)))))
