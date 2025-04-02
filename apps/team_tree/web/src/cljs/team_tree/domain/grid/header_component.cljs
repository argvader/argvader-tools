(ns team-tree.domain.grid.header-component
  (:require [stylefy.core :refer [use-style]]
            [router.core :refer [url-for]]
            [re-frame.core :as re-frame :refer [subscribe]]
            [team-tree.domain.subscriptions]
            [team-tree.domain.grid.styles :as styles]))

(defn component [team]
  (let [team-id (:id team)]
    (with-meta
      [:a (conj (use-style styles/link) {:href (url-for :team :id team-id)})
         [:div (use-style styles/title) (str (:name team))]]
      {:key team-id})))
