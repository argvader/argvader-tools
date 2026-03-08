(ns fable.export.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :export/pdf-url     (fn [db _] (:export/pdf-url db)))
(reg-sub :export/print-order (fn [db _] (:export/print-order db {})))
(reg-sub :export/print-form  (fn [db _] (:export/print-form db {})))
(reg-sub :export/pdf-error   (fn [db _] (:export/pdf-error db)))
(reg-sub :export/print-error (fn [db _] (:export/print-error db)))
