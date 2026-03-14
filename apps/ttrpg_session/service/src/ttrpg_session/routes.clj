(ns ttrpg-session.routes
  (:require
   [ttrpg-session.middleware :as mw]
   [ttrpg-session.handlers.health :as health]
   [ttrpg-session.handlers.session :as session]))

(defn routes [db]
  (let [protected [mw/json-body mw/bot-auth]]
    #{["/health"               :get  health/health-handler                             :route-name :health]
      ["/sessions"             :get  (conj protected (session/list-handler db))        :route-name :sessions-list]
      ["/sessions/:id"         :get  (conj protected (session/get-handler db))         :route-name :sessions-get]
      ["/sessions/:id/publish" :post (conj protected (session/publish-handler db))     :route-name :sessions-publish]
      ["/roster/reload"        :post (conj protected (session/reload-roster-handler))  :route-name :roster-reload]}))
