(ns contentful.utils.slack-formatter)

(defn format-text [data]
   (map #(conj {} {:type "section" :text {:type "mrkdwn" :text %}}) data))

(defn format-ephemeral [data]
   (map #(conj {} {:type "section" :text {:type "mrkdwn" :text %}}) data))

(defn format-error [data]
  (map #(conj {} {:type "section" :text {:type "mrkdwn" :text %}}) data))
