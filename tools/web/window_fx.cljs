(ns web.window-fx
  (:require [re-frame.core :as re-frame :refer [reg-event-fx reg-fx dispatch]]))

(defn- setup-observer! [handler element]
  (let [observer (js/ResizeObserver. handler)]
    (.observe observer element)))

(defn- on-resize [key entry]
  (dispatch (vec (concat key entry))))

(reg-fx
  ::on-resize
  (fn [{:keys [:dispatch :element] :as config}]
    (setup-observer! (partial on-resize dispatch) element)))
