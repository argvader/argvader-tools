(ns fable.wizard.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [fable.wizard.model :as model]
   [router.core :as router]))

(reg-event-fx
 :wizard/advance
 (fn [{:keys [db]} _]
   (let [current (:wizard/step db)
         next    (model/next-step current)]
     (if next
       {:db       (assoc db :wizard/step next)
        :dispatch [:router/navigate (name next)]}
       {:db db}))))

(reg-event-fx
 :wizard/back
 (fn [{:keys [db]} _]
   (let [current (:wizard/step db)
         prev    (model/prev-step current)]
     (if prev
       {:db       (assoc db :wizard/step prev)
        :dispatch [:router/navigate (name prev)]}
       {:db db}))))

(reg-event-db
 :wizard/set-step
 (fn [db [_ step]]
   (assoc db :wizard/step step)))
