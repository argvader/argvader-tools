(ns fable.effects
  (:require
   [re-frame.core :refer [reg-fx]]))

(reg-fx
 :navigate-to
 (fn [url]
   (set! (.-href js/window.location) url)))

(reg-fx
 :pwa-prompt-install
 (fn [deferred-event]
   (when deferred-event
     (.prompt deferred-event))))

(reg-fx
 :open-url
 (fn [url]
   (.open js/window url "_blank")))
