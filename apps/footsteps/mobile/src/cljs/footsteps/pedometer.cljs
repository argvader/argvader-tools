(ns footsteps.pedometer
  (:require
    ["expo-sensors" :refer [Pedometer]]
    [re-frame.core :as rf]
    [cljs-time.core :as time]
    [cljs-time.coerce :refer [to-date]]
    [promise.core :refer [promise->]]
    [footsteps.events]))

(defn- compare-steps [current next]
  (if-not (= current next)
    (rf/dispatch [:steps next])))

(rf/reg-event-db
  :check-steps
  (fn [db [_ _]]
    (let [now (to-date (time/now))
          start (to-date (:start db))]
      (-> (.getStepCountAsync Pedometer start now)
          (.then (fn [^js/Object result] (compare-steps (:steps db) (.-steps result))))))
    db))

(rf/reg-event-fx
  :watch-steps
  (fn []
    {:dispatch-interval {:dispatch [:check-steps]
                         :id :check-step-interval
                         :ms 1000}}))

(defn watch-steps []
  ;(rf/dispatch [:watch-steps]))
  (.watchStepCount Pedometer (fn [^js/Object result] (rf/dispatch [:steps (.-steps result)]))))
