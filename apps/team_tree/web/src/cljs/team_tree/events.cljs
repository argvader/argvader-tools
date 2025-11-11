(ns team-tree.events
  (:require [re-frame.core :as re-frame :refer [reg-event-db]]
            [cljs-time.core :as time]))

(reg-event-db
  :initialize-app
  (fn [db _]
    (assoc db :hello "Product Development")))
