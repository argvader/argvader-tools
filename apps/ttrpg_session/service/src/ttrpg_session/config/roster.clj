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

(defn character-name
  "Look up the character name for a Discord username. Returns nil if unmapped."
  [discord-username]
  (get-in (roster) [:players discord-username]))

(defn campaign-name []
  (:campaign (roster)))
