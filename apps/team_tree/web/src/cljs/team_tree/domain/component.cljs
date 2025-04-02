(ns team-tree.domain.component
  (:require [stylefy.core :refer [use-style]]
            [router.core :refer [url-for]]
            [re-frame.core :as re-frame :refer [subscribe]]
            [team-tree.loader.component :as loader]
            [team-tree.domain.subscriptions]
            [team-tree.role.utils :refer [role-sort]]
            [team-tree.domain.styles :as styles]
            [team-tree.domain.grid.header-component :as title]
            [team-tree.domain.leaders-component :as leaders]
            [team-tree.domain.grid.member-component :as member]))

(def member-model
  {:role "" :person {:first_name "" :last_name "" :id nil}})

(defn has-role? [member]
  (not (nil? (:person member))))

(defn- render-team-members [team size]
  (let [ordered-members (filter has-role? (role-sort (:members team)))
        padded-team (take size (concat ordered-members (repeat member-model)))]
    (map #(member/component %) padded-team)))

(defn- render-team [team size]
  (let [members (render-team-members team size)]
     [:<>
        [title/component team]
        (doall members)]))

(defn- render-teams [teams]
  (let [number-of-teams (count teams)
        max-team-size (apply max (map #(count (:members %)) teams))]
    (if (seq teams)
     [:div (use-style (styles/teams (inc max-team-size) number-of-teams))
       (map #(render-team % max-team-size) teams)])))

(defn- render-leadership [domain]
  [:div
     [:div
        (get-in domain [:product_lead :first_name])]])

(defn component []
  (let [domain (subscribe [:team-tree.domain.subscriptions/domain])]
    (fn []
      [:div (use-style styles/domain)
        (if (seq @domain)
           [:<>
             [:h1
               (:name @domain)]
             [leaders/component @domain]
             (render-teams (:teams @domain))]
           [:div (use-style styles/loading)
             [loader/component]])])))
