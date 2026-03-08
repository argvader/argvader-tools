(ns fable.media.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :media/posts        (fn [db _] (:media/posts db [])))
(reg-sub :media/selected-ids (fn [db _] (:media/selected-ids db #{})))

(reg-sub
 :media/selected-posts
 :<- [:media/posts]
 :<- [:media/selected-ids]
 (fn [[posts ids] _]
   (filter #(contains? ids (:id %)) posts)))
