(ns team-tree.header.subscriptions
  [:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]])

(reg-sub
  ::hello
  (fn [db _]
    (:hello db)))
