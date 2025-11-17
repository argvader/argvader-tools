(ns thank-ewe.utils.authorizer
  (:require-macros [adzerk.env :as env])
  (:require ["crypto" :as crypto]))

(env/def THANK_EWE_SLACK_SECRET :required)

(defn- encode-secret [base-key]
  (let [hmac (.createHmac crypto "sha256" THANK_EWE_SLACK_SECRET)
        data (.update hmac base-key)]
    (.digest data "hex")))

(defn- valid-signature? [event]
  (let [headers (.-headers event)
        timestamp (aget headers "X-Slack-Request-Timestamp")
        signature (aget headers "X-Slack-Signature")
        base-key (str "v0:" timestamp ":" (.-body event))
        sig-key (str "v0=" (encode-secret base-key))]
    (= sig-key signature)))

(defn- valid-timestamp? [event]
  (let [headers (.-headers event)
        timestamp (some-> (aget headers "X-Slack-Request-Timestamp") js/parseInt)
        tolerance (* 5 60)
        now (/ (.now js/Date) 1000)]
    (< (Math/abs (- now timestamp)) tolerance)))

(defn authorize [event]
  (and (valid-timestamp? event)
       (valid-signature? event)))

