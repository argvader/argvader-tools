(ns footsteps.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx after]]
   [clojure.spec.alpha :as s]
   [footsteps.sfx.core :as sfx]
   [footsteps.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
  :initialize-db
  validate-spec
  (fn [_ _]
    app-db))

(reg-event-db
  :steps
  (fn [db [_ count]]
    (sfx/play-sound (:active-sfx db) count)
    (js/console.log "sound " (:active-sfx db))
    (assoc db :counter count)))

(reg-event-db
  :select-sound
  (fn [db [_ sfx]]
    (js/console.log "selecting " sfx)
    (assoc db :active-sfx sfx)))
