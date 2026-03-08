(ns fable.auth.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [day8.re-frame.http-fx]
   [fable.http :as http]))

(reg-event-fx
 :auth/check-status
 (fn [_ _]
   {:http-xhrio (http/get-request "/auth/status"
                                  [:auth/status-success]
                                  [:auth/status-failure])}))

(reg-event-db
 :auth/status-success
 (fn [db [_ resp]]
   (-> db
       (assoc :auth/token-present? (:connected? resp))
       (assoc :auth/user {:name (:user-name resp)}))))

(reg-event-db
 :auth/status-failure
 (fn [db _]
   (assoc db :auth/token-present? false)))

(reg-event-fx
 :auth/connect-meta
 (fn [_ _]
   {:navigate-to "/auth/meta/init"}))

(reg-event-db
 :auth/callback-success
 (fn [db _]
   (assoc db :auth/token-present? true)))

(reg-event-fx
 :auth/logout
 (fn [_ _]
   {:http-xhrio (http/post-request "/auth/logout" {}
                                   [:auth/logout-success]
                                   [:auth/logout-failure])}))

(reg-event-db
 :auth/logout-success
 (fn [db _]
   (assoc db :auth/token-present? false :auth/user {})))

(reg-event-db
 :auth/logout-failure
 (fn [db _] db))
