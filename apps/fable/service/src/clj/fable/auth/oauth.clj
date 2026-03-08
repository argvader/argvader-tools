(ns fable.auth.oauth
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defn- meta-config []
  (:meta env/config))

(defn- app-id []     (get-in env/config [:meta :app-id]))
(defn- app-secret [] (get-in env/config [:meta :app-secret]))
(defn- redirect-uri [] (get-in env/config [:meta :redirect-uri]))
(defn- scope []      (get-in env/config [:meta :scope]))

(defn auth-url [state]
  (str "https://www.facebook.com/v18.0/dialog/oauth"
       "?client_id=" (app-id)
       "&redirect_uri=" (java.net.URLEncoder/encode (redirect-uri) "UTF-8")
       "&scope=" (scope)
       "&state=" state
       "&response_type=code"))

(defn exchange-code-for-token [code]
  (let [resp (http/post "https://graph.facebook.com/v18.0/oauth/access_token"
                        {:form-params {:client_id     (app-id)
                                       :client_secret (app-secret)
                                       :redirect_uri  (redirect-uri)
                                       :code          code}
                         :as :json})]
    (get-in resp [:body :access_token])))

(defn exchange-for-long-lived-token [short-lived-token]
  (let [resp (http/get "https://graph.facebook.com/v18.0/oauth/access_token"
                       {:query-params {:grant_type        "fb_exchange_token"
                                       :client_id         (app-id)
                                       :client_secret     (app-secret)
                                       :fb_exchange_token short-lived-token}
                        :as :json})]
    (get-in resp [:body :access_token])))

(defn fetch-user-profile [access-token]
  (let [resp (http/get "https://graph.facebook.com/v18.0/me"
                       {:query-params {:access_token access-token
                                       :fields       "id,name"}
                        :as :json})]
    (:body resp)))
