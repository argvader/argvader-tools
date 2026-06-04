(ns ttrpg-session.config.roster
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defonce ^:private cache (atom nil))

(defn- load! []
  (let [r (edn/read-string (slurp (io/resource "roster.edn")))]
    (reset! cache r)
    r))

(defn roster []
  (or @cache (load!)))

(defn reload! []
  (load!)
  (println "Roster reloaded"))

(def ^:private dm-label "Dungeon Master")

(defn dungeon-master
  "Discord username of the session facilitator (the DM), from roster.edn :dungeon-master.
   The DM voices NPCs/narration and is never listed as a player character. nil if unset."
  []
  (:dungeon-master (roster)))

(defn character-name
  "Display name for a Discord username: the mapped player-character name, the
   \"Dungeon Master\" label for the facilitator, or nil if unmapped (transcripts
   then fall back to the raw Discord name)."
  [discord-username]
  (cond
    (= discord-username (dungeon-master)) dm-label
    :else (get-in (roster) [:players discord-username])))

(defn campaign-name []
  (:campaign (roster)))

(defn custom-theme
  "Theme alias from roster.edn (:custom_theme), e.g. \"desert\". nil if unset."
  []
  (:custom_theme (roster)))

(defn all-pcs
  "Returns the player characters for the wiki — strictly the roster :players
   mappings, with the DM excluded even if mistakenly listed there. Each entry is
   {:discord-username str :character-name str}."
  []
  (let [dm (dungeon-master)]
    (->> (:players (roster))
         (remove (fn [[k _]] (= (name k) dm)))
         (map (fn [[k v]] {:discord-username (name k) :character-name v}))
         (sort-by :character-name))))
