(ns footsteps.app
  (:require
   [footsteps.styles :as styles]
   ["expo" :as ex]
   ["react-native" :as rn]
   ["react" :as react]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [district0x.re-frame.interval-fx :as int-fx]
   [shadow.expo :as expo]
   [footsteps.events]
   [footsteps.subscriptions]
   [footsteps.pedometer :as p]
   [footsteps.settings.component :as Settings]))

;; must use defonce and must refresh full app so metro can fill these in
;; at live-reload time `require` does not exist and will cause errors
;; must use path relative to :output-dir
(defonce splash-img (js/require "../assets/footsteps.jpg"))

(defn root []
  (let [counter (rf/subscribe [:get-counter])]
    (fn []
      [:> rn/View {:style styles/container}
       [:> rn/Text {:style styles/title} "Steps: " @counter]
       [:> rn/Image {:source splash-img :style {:width 200 :height 200}}]
       [Settings/component]])))


(defn start
  {:dev/after-load true}
  []
  (expo/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (p/watch-steps)
  (start))
