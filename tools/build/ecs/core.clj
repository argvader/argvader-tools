(ns build.ecs.core
  (:require [env.core                             :as env-config]
            [env.config                           :as env]
            [clojure.string                       :as str]
            [build.aws                            :refer [get-config set-profile!]]
            [build.ecs.cluster                    :as cluster]
            [build.ecs.task                       :as task]
            [build.ecs.service                    :as service]
            [build.ecs.alb                        :as alb]))
(defn args?
  [args] (when (seq args) args))

(defn -main [& args]
  (let [config (get-config)]
    (set-profile!)
    (cluster/upsert-cluster config)
    (task/clear-old-tasks (task/latest-task-version (:service config)) config)
    (let [running-task (task/create-task config)]
      (Thread/sleep 3000)
      (-> (alb/upsert-load-balancer config running-task)
          (get-in [:target-group :target-group-arn])
          (service/upsert-service config running-task)))))
