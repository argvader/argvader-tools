(ns ttrpg-session.middleware
  (:require
   [clojure.java.io :as io]
   [io.pedestal.interceptor :refer [interceptor]]
   [cheshire.core :as json]
   [env.config :as env]))

(def json-body
  (interceptor
   {:name  ::json-body
    :enter (fn [ctx]
             (let [req    (:request ctx)
                   ct     (get-in req [:headers "content-type"] "")
                   body   (:body req)
                   parsed (when (and body (re-find #"application/json" ct))
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

(def bot-auth
  (interceptor
   {:name  ::bot-auth
    :enter (fn [ctx]
             (let [req      (:request ctx)
                   secret   (get-in req [:headers "x-bot-secret"])
                   expected (get-in env/config [:discord :bot-secret])]
               (if (and secret (= secret expected))
                 ctx
                 (assoc ctx :response {:status  401
                                       :headers {"Content-Type" "application/json"}
                                       :body    (json/generate-string {:error "Unauthorized"})}))))}))
