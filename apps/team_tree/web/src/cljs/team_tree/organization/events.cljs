(ns team-tree.organization.events
  (:require [re-frame.core :as re-frame]
            [web.window-fx]))

(re-frame/reg-event-db
  ::resized
  (fn [db [_ _]]
    (let [uuid (str (random-uuid))]
      (assoc db :organization-resized uuid))))


(re-frame/reg-event-db
  ::organization
  (fn [db [_ {:keys [data errors] :as payload}]]
    (assoc db :organization (get-in payload [:data :domains]))))

(re-frame/dispatch
  [:re-graph.core/query
   "teams ($id: Int) { domains { id
    name
    teams {
      id
      members {
       person {
        id
       }
      }
    }}}"
    {}
    [:team-tree.organization.events/organization]])

(re-frame/reg-event-fx
  ::on-resize
  (fn []
   {:web.window-fx/on-resize {:dispatch [:team-tree.organization.events/resized]
                              :element (.querySelector js/document "#graph")}}))
