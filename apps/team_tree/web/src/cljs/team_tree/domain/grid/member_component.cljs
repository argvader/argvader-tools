(ns team-tree.domain.grid.member-component
  (:require [stylefy.core :refer [use-style]]
            [router.core :refer [url-for]]
            [re-frame.core :as re-frame :refer [subscribe]]
            [team-tree.domain.subscriptions]
            [team-tree.domain.grid.styles :as styles]))

(defn- render-member-block [label]
  (vector :div (use-style styles/block) label))

(defn component [member]
  (let [person (:person member)
        full-name-title (str (:first_name person)  " " (:last_name person) " (" (:job_level person) ")")]
    (with-meta
      (vector :div (use-style (styles/block member))
        full-name-title
        [:aside (use-style (styles/role member)) (:role member)]
        (if-not (nil? (:metrics person))
          [:div (use-style styles/metrics) (get-in person [:metrics :repository_count])]))
      {:key (:id person)})))
