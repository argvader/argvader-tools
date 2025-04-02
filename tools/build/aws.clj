(ns build.aws
  (:require [amazonica.core :refer [defcredential]]
            [clojure.pprint :as pp]
            [env.core       :as env-config]
            [env.config     :as env]))

(def env-flag (or (System/getenv "ENV") "dev"))

(defn build-tags
  ([tag-map]
   (build-tags tag-map "argvader"))
  ([tag-map name]
   (conj (reduce-kv (fn [m k v] (conj m {:key k :value v}))
            [] tag-map)
         {:key "Name" :value name})))

(defn set-profile! []
  (let [config (env-config/read-env env-flag)]
    (if (= env-flag "dev")
      (defcredential (:creds config)))))

(defn get-config []
  (let [config (env-config/read-env env-flag)]
    config))
