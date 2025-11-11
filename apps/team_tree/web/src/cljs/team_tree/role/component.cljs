(ns team-tree.role.component
  (:require [stylefy.core :refer [use-style sub-style]]
            [team-tree.role.styles :refer [style random-icon]]
            [reagent.core :as r]
            [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [team-tree.role.subscriptions]))

(def offset 28)

(defn- find-center [position]
    {:center-x (- (first position) offset)
     :center-y (- (last position) offset)})


(defn component [member location]
  (let [icon (random-icon)]
    (fn [member location]
      [:div {:style (merge icon style {:top (str (:center-y (find-center location)) "px")
                                       :left (str (:center-x (find-center location)) "px")})}
         [:aside (use-style (sub-style style :label))
            (get-in member [:person :first_name])]])))
