(ns contentful.features.check-ad-group
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [contentful.utils.axios :refer [axios-get]]
            [contentful.utils.slack-formatter :refer [format-ephemeral]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as str]))

(def ad-group-url "https://solutoconnect.prod.asurion.net/security/user-groups?user=")
(def snow-link "https://asurion.service-now.com/serviceportal?id=sc_cat_item&sys_id=4d1a5f0d37d28f4091988ff1b3990e7e")
(def ad-group-name "Mgmt-US-N-Contentful-CMS")

(defn- has-ad-group? [data]
  (let [ad-list (get-in data [:data :AD :allGroups])]
    (if (empty? (filter #(= % ad-group-name) ad-list))
      false
      true)))

(defn ad-member? [email]
  (go
    (try
      (let [name-part (first (str/split email #"@"))
            response (<p! (axios-get (str ad-group-url name-part)))]
        (has-ad-group? (js->clj response :keywordize-keys true)))
      (catch js/Error error false))))

(defn missing-group-message [email]
  (let [title (str "*Sorry, " email " does not have required AD group*")
        submit-help (str "Please add the AD Group: " ad-group-name)
        submit-link (str "using SNOW ticket: " snow-link)]
    (format-ephemeral [title submit-help submit-link])))

(defn reminder-group-message [email]
  (let [title (str ":reminder_ribbon: *" email " will need required AD group*")
        submit-help (str "Required AD Group: " ad-group-name)
        submit-link (str "using SNOW ticket: " snow-link)]
    (concat
       [{:type "divider"}]
       (format-ephemeral [title submit-help submit-link]))))
