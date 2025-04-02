(ns team-tree.role.subscriptions
  [:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]])

(reg-sub
  ::role
  (fn [db _]
    (:role db)))
