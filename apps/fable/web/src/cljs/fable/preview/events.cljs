(ns fable.preview.events
  (:require [re-frame.core :refer [reg-event-db]]))

(reg-event-db
 :preview/next-page
 (fn [db _]
   (let [pages (count (get-in db [:storybook/current :pages] []))
         idx   (:preview/page-index db 0)]
     (assoc db :preview/page-index (min (inc idx) (dec pages))))))

(reg-event-db
 :preview/prev-page
 (fn [db _]
   (let [idx (:preview/page-index db 0)]
     (assoc db :preview/page-index (max (dec idx) 0)))))

(reg-event-db
 :preview/goto-page
 (fn [db [_ n]]
   (assoc db :preview/page-index n)))
