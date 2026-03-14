(ns ttrpg-session.pipeline.merger
  (:require [clojure.string :as str]))

(defn merge-transcripts
  "Merge per-speaker Whisper verbose_json transcripts into a chronological sequence.

   tracks: seq of {:discord-username str :character-name str :transcript whisper-resp}
   Returns: [{:start double :character-name str :discord-username str :text str}]

   All per-user buffers start at the same wall-clock moment (/scribe start),
   so Whisper's relative segment :start values share the same time base."
  [tracks]
  (->> tracks
       (mapcat (fn [{:keys [character-name discord-username transcript]}]
                 (let [display-name (or character-name discord-username)]
                   (->> (:segments transcript)
                        (map (fn [seg]
                               {:start            (:start seg)
                                :character-name   display-name
                                :discord-username discord-username
                                :text             (str/trim (:text seg))}))))))
       (sort-by :start)))
