(ns team-tree.app
  (:require [env.config :as env]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as re-frame]
            [re-graph.core :as re-graph]
            [stylefy.core :as stylefy]
            [themes.typography :as typography]
            [team-tree.routes :as routes]
            [team-tree.events]))

(defn init []
  (stylefy/init {:use-caching? false})
  (typography/load-fonts)
  (re-frame/dispatch-sync [:re-graph.core/init {:ws-url                  false
                                                :http-url                (:graphapi env/config)
                                                :http-parameters         {:with-credentials? false
                                                                          :oauth-token "Secret"}
                                                :ws-reconnect-timeout    nil
                                                :resume-subscriptions?   false
                                                :connection-init-payload {}}])

  (re-frame/dispatch-sync [:initialize-app])
  (routes/mount))
