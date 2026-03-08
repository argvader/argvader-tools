(ns fable.media.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [day8.re-frame.http-fx]
   [fable.http :as http]))

(reg-event-fx
 :media/fetch
 (fn [_ _]
   {:http-xhrio (http/post-request "/media/fetch" {}
                                   [:media/fetch-success]
                                   [:media/fetch-failure])}))

(reg-event-db
 :media/fetch-success
 (fn [db [_ resp]]
   (assoc db :media/posts (:posts resp []))))

(reg-event-db
 :media/fetch-failure
 (fn [db _]
   (assoc db :media/posts [])))

(reg-event-db
 :media/toggle-selection
 (fn [db [_ post-id]]
   (update db :media/selected-ids
           (fn [ids]
             (if (contains? ids post-id)
               (disj ids post-id)
               (conj ids post-id))))))

(reg-event-db
 :media/clear-selection
 (fn [db _]
   (assoc db :media/selected-ids #{})))
