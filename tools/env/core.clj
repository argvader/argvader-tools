(ns env.core
   (:require [aero.core :as aero]
             [env.config :as env]
             [clojure.java.io :as io]))

(defn set-env! [app-config]
  (alter-var-root #'env.config/config (constantly app-config))
  app-config)

(defn read-env [release-flag]
  (-> (io/resource "config.edn")
      (aero/read-config {:profile (keyword release-flag)
                         :resolver aero/resource-resolver})
      (assoc :release-flag release-flag)))
