(ns team-tree.routes
  (:require [env.config :as env]
            [reagent.core :as reagent]
            [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [router.core :as router]
            [web.error-boundary :refer [error-boundary]]
            [team-tree.view.organization :refer [organization-view]]
            [team-tree.view.domain :refer [domain-view]]
            [team-tree.view.not-found :refer [not-found-view]]
            [team-tree.view.design-system :refer [design-system-view]]
            [team-tree.view.journey-team :refer [journey-team-view]]
            [team-tree.view.animation :refer [demo-view]]
            [team-tree.view.error :refer [error-view]]))

(defmulti views (fn [route] (get route :handler)))
(defmethod views :home [_] [organization-view])
(defmethod views :domain [route] [domain-view route])
(defmethod views :team [route] [journey-team-view route])
(defmethod views :demo [_] [demo-view])
(defmethod views :design-system [_] [design-system-view])
(defmethod views :not-found [_] [not-found-view])
(defmethod views :error [_] [error-view])
(defmethod views :default [_] [:div])

(def routes ["/" {"" :home
                  "domains/" { "" :domains [:id ""] :domain}
                  "demo" :demo
                  "teams/" { [:id ""] :team}
                  "design-system" :design-system
                  true   :not-found}])

(defn active-view []
  (let [route (re-frame/subscribe [:router.events/active-view])]
    (fn []
      (views @route))))

(defn mount []
  (router/init-routes routes)
  (let [catch-error (error-boundary error-view)]
    (re-frame/clear-subscription-cache!)
    (rdom/render [catch-error [active-view]]
                 (js/document.getElementById "application"))))
