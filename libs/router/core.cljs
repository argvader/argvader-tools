(ns router.core
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [router.events :as events]
            [re-frame.core :as re-frame]))

(def ^:dynamic routes (atom [true :not-found]))

(defn- dispatch-route [matched-route]
  (let [view-name (keyword (str (name (:handler matched-route))))]
    (re-frame/dispatch [:router.events/set-active-view matched-route])))

(defn- parse-url [url]
  (bidi/match-route @routes url))

(defn- parse-path []
  (let [path (-> js/window .-location .-pathname)]
    (:route-params (parse-url path))))

(defn init-routes [app-routes]
  (reset! routes app-routes)
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(defn init-middleware [middleware]
  (events/register-middleware middleware))

(defn query-params
  ([] (parse-path))
  ([key] (get (parse-path) key)))

(defn url-for [& args]
  (let [part (partial bidi/path-for @routes)]
    (apply part args)))
