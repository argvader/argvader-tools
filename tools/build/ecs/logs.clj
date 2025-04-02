(ns build.ecs.logs
  (:require [clojure.string                       :as str]
            [amazonica.aws.logs                   :as logs]
            [build.aws                            :refer [build-tags]]))


(defn- created? [group-name]
  (-> (logs/describe-log-groups {:log-group-name-prefix group-name})
      (:log-groups)
      (empty?)
      (not)))

(defn create-log-group [group-name]
  (println (logs/describe-log-groups {:log-group-name-prefix group-name}))
  (if-not (created? group-name)
    (logs/create-log-group {:log-group-name group-name})))
