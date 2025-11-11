(ns router.events
    (:require [re-frame.core :as re-frame]))

(def ^:dynamic middleware (atom []))

(re-frame/reg-sub
  ::active-view
  (fn [db _]
    (:active-view db)))

(re-frame/reg-event-db
  ::set-active-view
  @middleware
  (fn [db [_ view]]
    (assoc db :active-view view)))

(defn add-middleware [m]
  (swap! middleware conj m))

(defn register-middleware [list]
  (reset! middleware list))
