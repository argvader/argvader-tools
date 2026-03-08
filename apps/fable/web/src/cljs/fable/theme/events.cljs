(ns fable.theme.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db
 :theme/select
 (fn [db [_ theme]]
   (assoc db :theme/selected theme)))

(reg-event-fx
 :theme/confirm
 (fn [{:keys [db]} _]
   (if (:theme/selected db)
     {:dispatch [:wizard/advance]}
     {:db db})))
