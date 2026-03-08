(ns fable.export.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]
   [day8.re-frame.http-fx]
   [fable.http :as http]))

(reg-event-fx
 :export/download-pdf
 (fn [{:keys [db]} _]
   (let [id (get-in db [:storybook/current :id])]
     {:http-xhrio (http/get-request (str "/story/" id "/pdf")
                                    [:export/pdf-ready]
                                    [:export/pdf-error])})))

(reg-event-fx
 :export/pdf-ready
 (fn [{:keys [db]} [_ resp]]
   {:db       (assoc db :export/pdf-url (:url resp))
    :open-url (:url resp)}))

(reg-event-db
 :export/pdf-error
 (fn [db [_ err]]
   (assoc db :export/pdf-error err)))

(reg-event-fx
 :export/submit-print
 (fn [{:keys [db]} [_ order-details]]
   (let [id (get-in db [:storybook/current :id])]
     {:http-xhrio (http/post-request (str "/story/" id "/print")
                                     order-details
                                     [:export/print-submitted]
                                     [:export/print-error])})))

(reg-event-db
 :export/print-submitted
 (fn [db [_ resp]]
   (assoc db :export/print-order resp)))

(reg-event-db
 :export/print-error
 (fn [db [_ err]]
   (assoc db :export/print-error err)))

(reg-event-db
 :export/update-print-form
 (fn [db [_ field value]]
   (assoc-in db [:export/print-form field] value)))
