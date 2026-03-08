(ns fable.http
  (:require
   [env.config :as env]))

(defn- base-url []
  (or (:api-base env/config) "http://localhost:8888"))

(defn api-url [path]
  (str (base-url) path))

(defn get-request [path on-success on-failure]
  {:method          :get
   :uri             (api-url path)
   :with-credentials true
   :response-format {:read :json :key-fn keyword}
   :on-success      on-success
   :on-failure      on-failure})

(defn post-request [path body on-success on-failure]
  {:method          :post
   :uri             (api-url path)
   :with-credentials true
   :params          body
   :format          {:content-type "application/json" :write :json}
   :response-format {:read :json :key-fn keyword}
   :on-success      on-success
   :on-failure      on-failure})
