(ns footsteps.settings.component
  (:require
   [footsteps.settings.styles :as styles]
   [footsteps.sfx.component :as SoundEffects]
   ["expo" :as ex]
   ["react-native" :as rn]
   ["react" :as react]))

(defonce settings-img (js/require "../assets/gear_settings.png"))

(def height (:height (js->clj (.get rn/Dimensions "window") :keywordize-keys true)))
;(map-indexed #(with-meta %2  {:key %1}) children)]

(defn component []
  (fn []
    [:> rn/View {}
      [:> rn/View {:style styles/panel}
         [:> rn/View {:style styles/panel-header}
           [:> rn/View {:style styles/icon-background}
             [:> rn/ImageBackground {:source settings-img :style styles/icon}]]
           [:> rn/Text {:style styles/text-header} "Settings"]]
         [:> rn/View {:style styles/container}
           [:> rn/Text {:style styles/category} "Sounds"]
           [SoundEffects/component]]]]))
