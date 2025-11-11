(ns build.ecs.task
  (:require [clojure.string                       :as str]
            [amazonica.aws.servicediscovery       :as sd]
            [amazonica.aws.ecs                    :as ecs]
            [build.aws                            :refer [build-tags]])
  (:import (com.amazonaws.services.ecs.model LogDriver NetworkMode AssignPublicIp)
           (com.amazonaws.services.cloudwatchevents.model LaunchType)))

(defn- launch-task [task {:keys [cluster-name tags service]}]
  (->> (ecs/run-task
         {:cluster cluster-name
          :launch-type LaunchType/FARGATE
          :tags (build-tags tags)
          :task-definition (str (:name service) ":" (get-in task [:task-definition :revision]))
          :network-configuration {:aws-vpc-configuration {:subnets          ["subnet-982b27fd" "subnet-77e3833f"]
                                                          :assign-public-ip AssignPublicIp/DISABLED
                                                          :security-groups  ["sg-04d6c612478feeee4"]}}})
       (:tasks)
       (first)))

(defn- revision [arn]
  (-> arn
      (str/split #":")
      (last)
      (read-string)))

(defn- update-task-definition [task config]
  (ecs/register-task-definition task))

(defn latest-task-version [service]
  (let [definitions (ecs/list-task-definitions {:family-prefix (:name service)
                                                :status "ACTIVE"
                                                :sort "DESC"})]
    (if (empty? (:task-definition-arns definitions))
      "0"
      (-> definitions
          (:task-definition-arns)
          (first)
          (str/split #":")
          (last)
          (read-string)))))

(defn clear-old-tasks [current-revision {:keys [cluster-name service]}]
  (try
    (let [task-arns (ecs/list-tasks {:cluster cluster-name :service-name (:name service)})
          task-list (:tasks (ecs/describe-tasks {:cluster cluster-name :tasks (:task-arns task-arns)}))
          old-tasks (filter #(<= (revision (:task-definition-arn %)) current-revision) task-list)]
      (doseq [old-task task-list]
        (ecs/stop-task {:cluster cluster-name :task (:task-arn old-task)})
        (Thread/sleep 1000)))
    (catch Exception e)))

(defn create-task [config]
   (println (str "Create Task..."))
   (let [name (get-in config [:service :name])
         version (or (get-in config [:service :version]) "LATEST")
         task (:ecs-task-definition config)
         registry (:registry-namespace config)]
      (-> task
         (assoc :family name)
         (assoc :network-mode NetworkMode/Awsvpc)
         (assoc :tags (build-tags (:tags config)))
         (assoc-in [:container-definitions 0 :log-configuration :log-driver] LogDriver/Awslogs)
         (assoc-in [:container-definitions 0 :name] name)
         (assoc-in [:container-definitions 0 :image] (str (:account config) ".dkr.ecr.us-east-1.amazonaws.com/" name ":" version))
         (update-task-definition config))))
