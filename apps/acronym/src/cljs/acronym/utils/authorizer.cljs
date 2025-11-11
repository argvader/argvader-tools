(ns acronym.utils.authorizer
  (:require-macros [adzerk.env :as env])
  (:require ["crypto" :as crypto]))

(env/def ACRONYM_SECRET :required)

(defn- encode-secret [base-key]
  (let [hmac (.createHmac crypto "sha256" ACRONYM_SECRET)
        data (.update hmac base-key)
        digest (.digest data "hex")]
    digest))

(defn- valid-signature? [event]
  (let [headers (.-headers event)
        timestamp (aget headers "X-Slack-Request-Timestamp")
        signature (aget headers "X-Slack-Signature")
        base-key (str "v0:" timestamp ":" (.-body event))
        sig-key (str "v0=" (encode-secret base-key))]
    (= sig-key signature)))

(defn- valid-timestamp? [event]
  (let [headers (.-headers event)
        timestamp (aget headers "X-Slack-Request-Timestamp")
        minutes (* 3 60)
        now (/ (.now js/Date) 1000)]
    (< (Math/abs (- now timestamp)) minutes)))

(defn authorize [event]
  (and
      (valid-timestamp? event)
      (valid-signature? event)))
