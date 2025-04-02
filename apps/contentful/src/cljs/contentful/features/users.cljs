(ns contentful.features.users
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [contentful.gateways.contentful-api :as contentful-api]
            [cljs.core.async.interop :refer-macros [<p!]]))

(defn member? [email]
  (go
    (<! (contentful-api/registered? email))))

(defn invite [email]
  (go
    (<! (contentful-api/invite email))))

(defn handle-user-registration [email]
  (go
     (let [member (<! (member? email))]
       (if-not member
         (<! (invite email))))))
