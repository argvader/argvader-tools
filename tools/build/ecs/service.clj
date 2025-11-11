(ns build.ecs.service
  (:require [clojure.java.io                      :as io]
            [clojure.string                       :as str]
            [amazonica.aws.ecs                    :as ecs]
            [build.ecs.sd                         :as sd]
            [build.ecs.task                       :as task]
            [build.ecs.logs                       :as logs]
            [build.aws                            :refer [build-tags]])
  (:import (com.amazonaws.services.ecs.model NetworkMode AssignPublicIp)
           (com.amazonaws.services.cloudwatchevents.model LaunchType)))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- handle-upsert-service [latest-version target-group-arn {:keys [tags load-balancer ecs-task-definition cluster-name service]}]
  (println (str "Upsert service: " (:name service)))
  (let [name (:name service)
        network (get-in ecs-task-definition [:container-definitions 0 :port-mappings 0])
        services (:service-arns (ecs/list-services {:cluster cluster-name}))
        aws-service (filter #(str/includes? % name) services)
        config {:cluster cluster-name
                :launch-type LaunchType/FARGATE
                :task-definition (str name ":" latest-version)
                :desired-count 1
                :tags (build-tags tags name)
                :load-balancers [{
                                  :container-name name
                                  :target-group-arn target-group-arn
                                  :container-port (:container-port network)}]

                :network-configuration {:aws-vpc-configuration {:subnets          ["subnet-982b27fd" "subnet-77e3833f"]
                                                                :assign-public-ip AssignPublicIp/DISABLED
                                                                :security-groups  ["sg-04d6c612478feeee4"]}}}]
    (logs/create-log-group (str "/ecs/" (:name service)))
    (println "Completed Log Check")
    (if (empty? aws-service)
      (-> config
          (assoc :service-name name)
          (ecs/create-service))
      (-> config
          (assoc :service name)
          (ecs/update-service)))))

(defn upsert-service [target-group-arn config active-task]
  (let [latest-task (task/latest-task-version (:service config))
        service (handle-upsert-service latest-task target-group-arn config)]
     {:service service
      :target-group-arn target-group-arn
      :latest-task latest-task}))
