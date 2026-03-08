(ns fable.generate.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx dispatch]]
   [day8.re-frame.http-fx]
   [fable.http :as http]))

(reg-event-fx
 :generate/start
 (fn [{:keys [db]} _]
   (let [selected-ids (:media/selected-ids db)
         posts        (filter #(contains? selected-ids (:id %)) (:media/posts db))
         theme        (:theme/selected db)]
     {:http-xhrio (http/post-request "/story/generate"
                                     {:posts (vec posts) :theme (name theme)}
                                     [:generate/started]
                                     [:generate/error])})))

(reg-event-fx
 :generate/started
 (fn [{:keys [db]} [_ resp]]
   {:db       (assoc db :generate/job-id (:job-id resp) :generate/progress 0)
    :dispatch [:generate/poll]}))

(reg-event-fx
 :generate/poll
 (fn [{:keys [db]} _]
   (when-let [job-id (:generate/job-id db)]
     {:http-xhrio    (http/get-request (str "/story/job/" job-id)
                                       [:generate/poll-success]
                                       [:generate/error])
      :dispatch-later [{:ms 2000 :dispatch [:generate/schedule-poll]}]})))

(reg-event-fx
 :generate/schedule-poll
 (fn [{:keys [db]} _]
   (let [status (:generate/status db)]
     (when (not (contains? #{"done" "error"} status))
       {:dispatch [:generate/poll]}))))

(reg-event-fx
 :generate/poll-success
 (fn [{:keys [db]} [_ resp]]
   (let [status   (:status resp)
         progress (:progress resp 0)
         sb-id    (:storybook-id resp)]
     (cond-> {:db (assoc db
                         :generate/status   status
                         :generate/progress progress)}
       (= "done" status)
       (assoc :dispatch [:generate/complete sb-id])))))

(reg-event-fx
 :generate/complete
 (fn [{:keys [db]} [_ storybook-id]]
   {:http-xhrio (http/get-request (str "/story/" storybook-id)
                                  [:generate/storybook-loaded]
                                  [:generate/error])}))

(reg-event-fx
 :generate/storybook-loaded
 (fn [{:keys [db]} [_ storybook]]
   {:db       (assoc db :storybook/current storybook)
    :dispatch [:wizard/advance]}))

(reg-event-db
 :generate/error
 (fn [db [_ err]]
   (assoc db :generate/status "error" :generate/error err)))
