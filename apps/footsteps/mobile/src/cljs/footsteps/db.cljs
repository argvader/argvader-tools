(ns footsteps.db
  (:require [clojure.spec.alpha :as s]
            [cljs-time.core :as time]))


;; spec of app-db
(s/def ::counter number?)
(s/def ::app-db
  (s/keys :req-un [::counter]))

;; initial state of app-db
(defonce app-db {:counter 0 :active-sfx :heels :start (time/now)})
