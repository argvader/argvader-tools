(ns fable.preview.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :storybook/current   (fn [db _] (:storybook/current db)))
(reg-sub :preview/page-index  (fn [db _] (:preview/page-index db 0)))

(reg-sub
 :preview/current-page
 :<- [:storybook/current]
 :<- [:preview/page-index]
 (fn [[sb idx] _]
   (get-in sb [:pages idx])))
