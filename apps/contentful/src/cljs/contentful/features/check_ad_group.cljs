(ns contentful.features.check-ad-group
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [contentful.utils.axios :refer [axios-get]]
            [contentful.utils.slack-formatter :refer [format-ephemeral]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [clojure.string :as str]))

(def snow-link "https://argvader.service-now.com/serviceportal?id=sc_cat_item&sys_id=953a90d9db65c2d09490d8e4e2961989")

(defn reminder-group-message [email]
  (let [title (str ":reminder_ribbon: *" email " may need to be added*")
        submit-link (str "using SNOW ticket: " snow-link)]
    (concat
       [{:type "divider"}]
       (format-ephemeral [title submit-link]))))
