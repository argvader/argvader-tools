(ns footsteps.sfx.component
  (:require
   [footsteps.sfx.styles :as styles]
   ["react-native" :as rn]
   ["react" :as react]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defonce sounds [:heels :spurs])

(defn component []
  (let [sound-fx (rf/subscribe [:active-sound])]
    (fn []
      [:> rn/View {:style styles/container}
         [:> rn/TouchableOpacity {:style styles/button
                                  :on-press #(rf/dispatch [:select-sound :heels])}
            [:> rn/Text {:style styles/button-text} "Heels"]]
         [:> rn/TouchableOpacity {:style styles/button
                                  :on-press #(rf/dispatch [:select-sound :spurs])}
            [:> rn/Text {:style styles/button-text} "Spurs"]]])))
