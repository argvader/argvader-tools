(ns fable.app
  (:require
   [re-frame.core :as re-frame]
   [stylefy.core :as stylefy]
   [stylefy.reagent :as stylefy-reagent]
   [fable.routes :as routes]
   [fable.events]
   [fable.effects]
   [fable.wizard.events]
   [fable.auth.events]
   [fable.media.events]
   [fable.theme.events]
   [fable.generate.events]
   [fable.preview.events]
   [fable.export.events]))

(defn init []
  (stylefy/init {:dom (stylefy-reagent/init)})
  (re-frame/dispatch-sync [:initialize-app])
  (routes/mount))
