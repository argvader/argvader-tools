(ns team-tree.organization.subscriptions
  [:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]])

(reg-sub
  ::organization
  (fn [db _]
    (:organization db)))

(reg-sub
  ::resized
  (fn [db _]
    (:organization-resized db)))
