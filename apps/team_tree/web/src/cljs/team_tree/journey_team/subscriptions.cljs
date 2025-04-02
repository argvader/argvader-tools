(ns team-tree.journey-team.subscriptions
  (:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]
            [team-tree.journey-team.core :as jt]
            [team-tree.journey-team.model :as model]))

(reg-sub
  ::journey-team
  (fn [db _]
    (jt/->Journey-Team (:journey-team db))))
