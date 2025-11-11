(ns footsteps.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-counter
  (fn [db _]
    (:counter db)))

(reg-sub
  :active-sound
  (fn [db _]
    (:active-sfx db)))
