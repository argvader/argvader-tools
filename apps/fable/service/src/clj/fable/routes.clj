(ns fable.routes
  (:require
   [io.pedestal.http.body-params :as body-params]
   [fable.middleware :as mw]
   [fable.handlers.auth :as auth]
   [fable.handlers.media :as media]
   [fable.handlers.story :as story]
   [fable.handlers.pdf :as pdf]
   [fable.handlers.print :as print-handler]))

(defn- health [_request]
  {:status 200 :body "fable-api OK"})

(defn routes [db]
  (let [protected [mw/json-body mw/session-auth]
        json-only [mw/json-body]]
    #{["/health"                  :get    health                                    :route-name :health]
      ["/auth/meta/init"          :get    (auth/init-handler)                        :route-name :auth-meta-init]
      ["/auth/meta/callback"      :get    (auth/callback-handler db)                 :route-name :auth-meta-callback]
      ["/auth/logout"             :post   (conj protected (auth/logout-handler db))  :route-name :auth-logout]
      ["/auth/status"             :get    (conj protected (auth/status-handler))     :route-name :auth-status]
      ["/media/fetch"             :post   (conj protected (media/fetch-handler))     :route-name :media-fetch]
      ["/story/generate"          :post   (conj protected (story/generate-handler db)) :route-name :story-generate]
      ["/story/job/:job-id"       :get    (conj protected (story/job-handler db))   :route-name :story-job]
      ["/story/:id"               :get    (conj protected (story/get-handler db))   :route-name :story-get]
      ["/story/:id/pdf"           :get    (conj protected (pdf/generate-handler db)) :route-name :story-pdf]
      ["/story/:id/print"         :post   (conj protected (print-handler/submit-handler db)) :route-name :story-print]
      ["/story/:id/print-status"  :get    (conj protected (print-handler/status-handler db)) :route-name :story-print-status]}))
