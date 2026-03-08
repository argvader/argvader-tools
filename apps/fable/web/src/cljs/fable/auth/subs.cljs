(ns fable.auth.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :auth/token-present? (fn [db _] (:auth/token-present? db)))
(reg-sub :auth/user           (fn [db _] (:auth/user db)))
