(ns fable.theme.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :theme/selected (fn [db _] (:theme/selected db)))
