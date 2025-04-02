(ns team-tree.journey-team.events
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  ::on-team
  (fn [db [_ {:keys [data errors] :as payload}]]
    (assoc db :journey-team (merge {} (get-in payload [:data :team_by_id])))))

(defn find-team-event [team-id]
  (re-frame/dispatch
    [:re-graph.core/query
     "teams ($id: Int) { team_by_id  (id: $id) { id
      name
      members {
        role
        person {
          id
          first_name
          last_name
        }
      }}}"
      {:id (js/parseInt team-id)}
      [:team-tree.journey-team.events/on-team]]))
