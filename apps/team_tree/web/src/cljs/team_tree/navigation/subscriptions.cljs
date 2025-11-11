(ns team-tree.navigation.subscriptions
  [:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]])

(reg-sub
  ::active_view
  (fn [db _]
    (:active_view db)))
