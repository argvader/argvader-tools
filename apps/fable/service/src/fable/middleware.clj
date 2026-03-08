(ns fable.middleware
  (:require
   [clojure.java.io :as io]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.http.body-params :as body-params]
   [cheshire.core :as json]
   [fable.auth.session :as session]))

(def json-body
  "Parse JSON request body and coerce response body to JSON."
  (interceptor
   {:name  ::json-body
    :enter (fn [ctx]
             (let [req     (:request ctx)
                   ct      (get-in req [:headers "content-type"] "")
                   body    (:body req)
                   parsed  (when (and body (re-find #"application/json" ct))
                             (json/parse-stream (io/reader body) true))]
               (cond-> ctx
                 parsed (assoc-in [:request :json-params] parsed))))
    :leave (fn [ctx]
             (let [body (get-in ctx [:response :body])]
               (if (map? body)
                 (-> ctx
                     (assoc-in [:response :body] (json/generate-string body))
                     (assoc-in [:response :headers "Content-Type"] "application/json"))
                 ctx)))}))

(def session-auth
  "Interceptor that validates the session cookie and attaches session to request."
  (interceptor
   {:name  ::session-auth
    :enter (fn [ctx]
             (let [req        (:request ctx)
                   session-id (session/get-session-id req)]
               (if-let [sess (and session-id (session/load-session session-id))]
                 (assoc-in ctx [:request :session] sess)
                 (assoc ctx :response {:status 401
                                       :headers {"Content-Type" "application/json"}
                                       :body (json/generate-string {:error "Unauthorized"})}))))}))
