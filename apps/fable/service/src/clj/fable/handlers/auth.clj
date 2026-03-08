(ns fable.handlers.auth
  (:require
   [cheshire.core :as json]
   [fable.auth.oauth :as oauth]
   [fable.auth.session :as session]
   [fable.db.store :as store]))

(defn- random-state []
  (str (java.util.UUID/randomUUID)))

(defn init-handler []
  (fn [request]
    (let [state (random-state)]
      {:status  302
       :headers {"Location"  (oauth/auth-url state)
                 "Set-Cookie" (str "fable-oauth-state=" state "; HttpOnly; Path=/; Max-Age=600")}
       :body    ""})))

(defn callback-handler [_db]
  (fn [request]
    (let [params       (:query-params request)
          code         (get params "code")
          _state       (get params "state")]
      (if-not code
        {:status 400 :body (json/generate-string {:error "Missing code"})}
        (try
          (let [short-token   (oauth/exchange-code-for-token code)
                long-token    (oauth/exchange-for-long-lived-token short-token)
                profile       (oauth/fetch-user-profile long-token)
                sess          (session/create-session!
                               {:meta-token (:access_token long-token long-token)
                                :user-name  (:name profile)
                                :user-id    (:id profile)})]
            (-> {:status  302
                 :headers {"Location" "http://localhost:3000/#/connect-success"}
                 :body    ""}
                (session/set-session-cookie (str (:id sess)))))
          (catch Exception e
            {:status 500
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error (.getMessage e)})}))))))

(defn logout-handler [_db]
  (fn [request]
    (let [session-id (session/get-session-id request)]
      (when session-id
        (store/delete-session! session-id))
      (-> {:status 200
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {:ok true})}
          session/clear-session-cookie))))

(defn status-handler []
  (fn [request]
    (let [sess (:session request)]
      {:status  200
       :headers {"Content-Type" "application/json"}
       :body    (json/generate-string
                 {:connected? (some? sess)
                  :user-name  (:user_name sess)})})))
