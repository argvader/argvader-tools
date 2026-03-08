(ns fable.generate.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :generate/job-id   (fn [db _] (:generate/job-id db)))
(reg-sub :generate/progress (fn [db _] (:generate/progress db 0)))
(reg-sub :generate/status   (fn [db _] (:generate/status db "pending")))
(reg-sub :generate/error    (fn [db _] (:generate/error db)))
