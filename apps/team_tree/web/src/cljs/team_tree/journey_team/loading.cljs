(ns team-tree.journey-team.loading
  (:require [re-frame.core :as re-frame :refer [reg-sub subscribe]]
            [reagent.core :as r]
            [stylefy.core :refer [use-style sub-style]]
            [team-tree.journey-team.core :as jt]
            [team-tree.journey-team.styles :as styles]
            [rx-animation.core :as animation]
            [rx-animation.component :as animated]
            [rx-animation.math :as rx-math]
            [rx-animation.timeline :as timeline]))

(def rotations 1)
(def loading-team (r/atom (jt/->Journey-Team {:name "Loading"
                                              :members [{:id "1" :person {:first_name "" :last_name ""} :role "engineer"}
                                                        {:id "2" :person {:first_name "" :last_name ""} :role "engineer"}
                                                        {:id "3" :person {:first_name "" :last_name ""} :role "engineer"}
                                                        {:id "4" :person {:first_name "" :last_name ""} :role "product lead"}
                                                        {:id "5" :person {:first_name "" :last_name ""} :role "UX lead"}]})))

(defn- rotate-team [frame]
  (let [degree (* rotations 360 frame)
        spinning-team (:team @loading-team)
        radius (:radius @loading-team)]
    (swap! loading-team assoc :team (jt/position-team spinning-team radius degree))))

(animation/create :loading-team {:timeline (->> (timeline/duration 6500)
                                                (timeline/looping))
                                 :on-frame (fn [frame]
                                               (rotate-team frame)
                                               {})})

(defn component[]
    [animated/component {:animation :loading-team}
       [:div (use-style (styles/team-background (:radius @loading-team)))
         [:div (use-style styles/label)
           (str (:name @loading-team)) " Team"]
         [:div (use-style (styles/style-team (:radius @loading-team)))
           (:team @loading-team)]]])
