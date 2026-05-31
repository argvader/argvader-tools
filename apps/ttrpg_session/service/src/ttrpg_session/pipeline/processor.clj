(ns ttrpg-session.pipeline.processor
  (:require
   [ttrpg-session.db.store :as store]
   [ttrpg-session.audio.decoder :as decoder]
   [ttrpg-session.audio.writer :as writer]
   [ttrpg-session.ai.whisper :as whisper]
   [ttrpg-session.ai.image :as image]
   [ttrpg-session.pipeline.merger :as merger]
   [ttrpg-session.ai.narrator :as narrator]
   [ttrpg-session.config.roster :as roster]
   [ttrpg-session.site.generator :as generator]
   [ttrpg-session.site.publisher :as publisher]
   [env.config :as env]))

(defn- transcribe-user!
  "Decode, downsample, write WAV, transcribe via Whisper, persist track row.
   Returns {:discord-username :character-name :transcript} for merging."
  [session-id user-id username pcm-bufs tmp-dir]
  (println (str "Transcribing user: " username))
  (let [raw-pcm   (decoder/concat-buffers pcm-bufs)
        mono-pcm  (decoder/downsample raw-pcm)
        wav       (writer/wav-file tmp-dir user-id)
        _         (writer/write-wav! mono-pcm wav)
        char-name (roster/character-name username)
        track     (store/create-speaker-track!
                   {:session-id       session-id
                    :discord-user-id  user-id
                    :discord-username username
                    :character-name   char-name})
        transcript (whisper/transcribe wav)]
    (store/update-track-transcript! (str (:id track)) transcript)
    {:discord-username username
     :character-name   char-name
     :transcript       transcript}))

(defn- parse-summary-jsonb
  "Parse all JSONB columns on a session_summaries row."
  [row]
  (-> row
      (update :key_events_detail  store/parse-jsonb)
      (update :memorable_moments  store/parse-jsonb)
      (update :lore_learned       store/parse-jsonb)
      (update :items              store/parse-jsonb)
      (update :mysteries          store/parse-jsonb)
      (update :commitments        store/parse-jsonb)
      (update :next_steps         store/parse-jsonb)
      (update :timeline_entries   store/parse-jsonb)
      (update :pc_moments         store/parse-jsonb)))

(defn- build-pc-moments-index
  "Returns a map of character-name -> [{:moment :session-title :session-number :date-slug}]
   from all session summaries."
  [raw-rows]
  (reduce
   (fn [acc row]
     (let [snum    (:session_number row)
           stitle  (:session_title row)
           started (:started_at row)
           ds      (.format ^java.time.OffsetDateTime started
                            java.time.format.DateTimeFormatter/ISO_LOCAL_DATE)
           moments (store/parse-jsonb (:pc_moments row))]
       (reduce
        (fn [a m]
          (update a (:character-name m) conj
                  {:moment         (:moment m)
                   :session-title  stitle
                   :session-number snum
                   :date-slug      ds}))
        acc
        moments)))
   {}
   raw-rows))

(defn process-session!
  "Orchestrate the full decode→transcribe→merge→summarize→publish pipeline.
   pcm-buffers: {user-id -> {:username str :pcm-bufs [byte[]]}}"
  [session-id pcm-buffers _db]
  (try
    (println (str "Processing session: " session-id))
    (let [session  (store/find-recording-session session-id)
          tmp-dir  (writer/tmp-dir! session-id)

          ;; 1. Transcribe all speakers
          tracks   (doall
                    (for [[user-id {:keys [username pcm-bufs]}] pcm-buffers
                          :when (seq pcm-bufs)]
                      (transcribe-user! session-id user-id username pcm-bufs tmp-dir)))
          merged   (merger/merge-transcripts tracks)

          ;; 2. Generate rich summary
          summary  (narrator/summarize merged (:campaign_name session))

          ;; 3. Generate and upload scene image
          _        (println "Generating scene image...")
          png      (image/generate-scene-image (:scene-image-prompt summary))
          img-path (publisher/publish-image! session-id png)

          ;; 4. Persist summary
          _        (store/create-summary!
                    (merge summary
                           {:session-id       session-id
                            :transcript       merged
                            :scene-image-path img-path}))

          ;; 5. Upsert NPCs
          _        (doseq [{:keys [slug name description session-notes]} (:npcs summary)]
                     (let [npc (store/upsert-npc! {:slug slug :name name :description description})]
                       (store/add-npc-session-note!
                        {:npc-id     (:id npc)
                         :session-id session-id
                         :notes      session-notes})))

          ;; 6. Upsert locations
          _        (doseq [{:keys [slug name description session-notes]} (:locations summary)]
                     (let [loc (store/upsert-location! {:slug slug :name name :description description})]
                       (store/add-location-session-note!
                        {:location-id (:id loc)
                         :session-id  session-id
                         :notes       session-notes})))

          ;; 7. Fetch full wiki state
          all-sessions  (store/list-sessions-with-titles)
          all-npcs      (store/list-npcs)
          all-locations (store/list-locations)
          pcs           (roster/all-pcs)

          ;; Session number index: session-id string -> Long
          session-num-idx (into {} (map (fn [s] [(str (:id s)) (:session_number s)])
                                        all-sessions))
          this-num        (get session-num-idx session-id)

          ;; 8. Build PC moments index from all sessions
          pc-moment-rows    (store/find-pc-moments-across-sessions)
          pc-moments-index  (build-pc-moments-index pc-moment-rows)

          ;; 9. Fetch and parse this session's summary row for generation
          summary-row  (parse-summary-jsonb (store/find-summary session-id))

          ;; 10. Generate session page
          session-page (generator/session-page session summary-row this-num)

          ;; 11. Generate NPC pages
          npc-pages    (mapv (fn [npc]
                               (generator/npc-page npc (store/find-notes-for-npc (:id npc))))
                             all-npcs)

          ;; 12. Generate location pages
          loc-pages    (mapv (fn [loc]
                               (generator/location-page loc (store/find-notes-for-location (:id loc))))
                             all-locations)

          ;; 13. Generate PC pages
          pc-pages     (mapv (fn [pc]
                               (generator/pc-page pc
                                 (get pc-moments-index (:character-name pc) [])))
                             pcs)

          ;; 14. Generate index, mkdocs.yml, workflow
          index        (generator/index-page (roster/campaign-name) all-sessions)
          site-url     (str "https://" (get-in env/config [:github :owner])
                            ".github.io/" (get-in env/config [:github :repo]) "/")
          mkdocs       (generator/mkdocs-yml
                        {:campaign-name (roster/campaign-name)
                         :site-url      site-url
                         :sessions      all-sessions
                         :npcs          all-npcs
                         :locations     all-locations
                         :pcs           pcs})
          workflow     (generator/github-workflow)

          ;; 15. Push all files
          all-files    (concat [session-page index mkdocs workflow]
                               npc-pages loc-pages pc-pages)
          _            (println (str "Publishing " (count all-files) " files..."))
          _            (publisher/publish-files! all-files)
          _            (store/update-summary-published! session-id (:path session-page))
          _            (store/complete-recording-session! session-id)]
      (println (str "Session " session-id " complete.")))
    (catch Exception e
      (println (str "Error processing session " session-id ": " (.getMessage e)))
      (store/fail-recording-session! session-id (.getMessage e)))))
