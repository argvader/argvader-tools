(ns rx-animation.core
  [:require [beicon.core :as core]
            [re-frame.core :as re-frame]
            [rx-animation.timeline :as timeline]
            [rx-animation.random-names :as names]])

; animation
;   timeline (timeline$)
;   targets (fn)
;   on-start (fn)
;   on-end (fn)
;   on-frame (fn [frame])
;   meta-data { :running bool :count number }
(defn on-start [name] (js/console.log (str name " started")))
(defn on-error [name error] (js/console.log error))
(defn on-end [name] (js/console.log (str name " ended")))
(defn on-frame [frame] (js/console.log (str " frame: " frame)))

(def default-config {:timeline (timeline/duration 1600)
                     :targets (fn [] [])
                     :on-start on-start
                     :on-end on-end
                     :on-error on-error
                     :on-frame on-frame})

(def animations (atom {}))

(defn play [key]
  (let [animation (get @animations key)
        name (:name animation)
        timeline (:timeline animation)
        frame-handler (:on-frame animation)
        end-handler (partial (:on-end animation) name)]
    (core/subscribe timeline
                    frame-handler
                    on-error
                    end-handler)))

(defn destroy [key]
  (swap! animations dissoc key))

(defn create
  ([]
   (create (names/generate) {}))
  ([config]
   (create (names/generate) config))
  ([name config]
   (let [key (if (keyword? name) name (keyword name))
         animation-config (merge default-config {:name key} config)]
    (swap! animations assoc key animation-config)
    animation-config)))
