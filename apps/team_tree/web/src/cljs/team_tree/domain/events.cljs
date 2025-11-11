(ns team-tree.domain.events
  (:require [re-frame.core :as re-frame :refer [reg-event-fx reg-event-db]]))

(re-frame/reg-event-db
  ::active-domain
  (fn [db [_ {:keys [data errors] :as payload}]]
    (assoc db :domain (merge {} (get-in payload [:data :domain_by_id])))))

(defn find-domain-event [domain-id]
  (re-frame/dispatch
    [:re-graph.core/query
     "domains ($id: Int) { domain_by_id  (id: $id) {
      id
      name
      product_lead {
        id
        first_name
        last_name
      }
      engineering_lead {
        id
        first_name
        last_name
      }
      design_lead {
        id
        first_name
        last_name
      }
      teams {
        id
        name
        members {
          role
          person
            {
             id
             first_name
             last_name
             job_level
             metrics {
               repository_count
             }
            }
        }
      }
      }}"
      {:id (js/parseInt domain-id)}
      [:team-tree.domain.events/active-domain]]))
