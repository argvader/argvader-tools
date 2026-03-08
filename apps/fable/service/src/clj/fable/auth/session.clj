(ns fable.auth.session
  (:require
   [buddy.sign.jwt :as jwt]
   [env.config :as env]
   [fable.db.store :as store]))

(defn- secret []
  (get-in env/config [:session :secret]))

(defn encrypt-token [token]
  (jwt/sign {:token token} (secret) {:alg :hs256}))

(defn decrypt-token [encrypted]
  (try
    (:token (jwt/unsign encrypted (secret) {:alg :hs256}))
    (catch Exception _ nil)))

(defn get-session-id [request]
  (let [cookies (get-in request [:headers "cookie"] "")]
    (second (re-find #"fable-session=([^;]+)" cookies))))

(defn set-session-cookie [response session-id]
  (assoc-in response [:headers "Set-Cookie"]
            (str "fable-session=" session-id
                 "; HttpOnly; SameSite=Lax; Path=/; Max-Age=5184000")))

(defn clear-session-cookie [response]
  (assoc-in response [:headers "Set-Cookie"]
            "fable-session=; HttpOnly; Path=/; Max-Age=0"))

(defn load-session [session-id]
  (store/find-session session-id))

(defn create-session! [{:keys [meta-token user-name user-id]}]
  (let [encrypted    (encrypt-token meta-token)
        expires-at   (.plusDays (java.time.OffsetDateTime/now java.time.ZoneOffset/UTC) 60)]
    (store/create-session! {:meta-token  encrypted
                            :user-name   user-name
                            :user-id     user-id
                            :expires-at  expires-at})))

(defn get-meta-token [session]
  (decrypt-token (:meta_token session)))
