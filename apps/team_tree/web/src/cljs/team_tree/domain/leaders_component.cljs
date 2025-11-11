(ns team-tree.domain.leaders-component
  (:require [clojure.string :as str]
            [stylefy.core :refer [use-style]]
            [router.core :refer [url-for]]
            [team-tree.domain.styles :as styles]
            [re-frame.core :as re-frame :refer [subscribe]]))

(defn- ->Key [name]
  (->> (str/split name #" ")
      (str/join "_")
      (str/lower-case)
      (keyword)))

(defn- name-plate [role domain]
  (let [name-key (->Key role)
        person (get domain name-key)]
    [:div (use-style styles/leader)
       (:first_name person)
       " "
       (:last_name person)
       [:aside (use-style styles/leader-role) role]]))

(defn component [domain]
  (fn [domain]
    [:div (use-style styles/leaders-container)
       (name-plate "Product Lead" domain)
       (name-plate "Design Lead" domain)
       (name-plate "Engineering Lead" domain)]))
