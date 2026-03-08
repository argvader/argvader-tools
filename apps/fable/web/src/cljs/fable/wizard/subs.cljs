(ns fable.wizard.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :wizard/step (fn [db _] (:wizard/step db)))
