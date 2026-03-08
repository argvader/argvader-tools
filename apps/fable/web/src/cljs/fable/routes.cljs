(ns fable.routes
  (:require
   [re-frame.core :as re-frame]
   [reagent.dom :as rdom]
   [router.core :as router]
   [fable.auth.views :refer [connect-view connect-success-view]]
   [fable.media.views :refer [media-view]]
   [fable.theme.views :refer [theme-view]]
   [fable.generate.views :refer [generate-view]]
   [fable.preview.views :refer [preview-view]]
   [fable.export.views :refer [export-view]]))

(defmulti views (fn [route] (:handler route)))
(defmethod views :connect         [_] [connect-view])
(defmethod views :connect-success [_] [connect-success-view])
(defmethod views :media           [_] [media-view])
(defmethod views :theme           [_] [theme-view])
(defmethod views :generate        [_] [generate-view])
(defmethod views :preview         [_] [preview-view])
(defmethod views :export          [_] [export-view])
(defmethod views :default         [_] [connect-view])

(def routes
  ["/" {"" :connect
        "connect-success" :connect-success
        "media"           :media
        "theme"           :theme
        "generate"        :generate
        "preview"         :preview
        "export"          :export}])

(defn active-view []
  (let [route (re-frame/subscribe [:router.events/active-view])]
    (fn [] (views @route))))

(defn mount []
  (router/init-routes routes)
  (re-frame/clear-subscription-cache!)
  (rdom/render [active-view]
               (js/document.getElementById "application")))
