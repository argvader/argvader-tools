(ns team-tree.domain.subscriptions
  [:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]])

(reg-sub
  ::domain
  (fn [db _]
    (:domain db)))
