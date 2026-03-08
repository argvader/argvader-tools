(ns fable.events
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx]]))

(reg-event-db
 :initialize-app
 (fn [_ _]
   {:wizard/step         :connect
    :auth/token-present? false
    :auth/user           {}
    :media/posts         []
    :media/selected-ids  #{}
    :theme/selected      nil
    :generate/job-id     nil
    :generate/progress   0
    :storybook/current   nil
    :preview/page-index  0
    :export/pdf-url      nil
    :export/print-order  {}}))
