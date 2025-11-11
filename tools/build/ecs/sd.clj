(ns build.ecs.sd
  (:require [clojure.java.io                      :as io]
            [clojure.string                       :as str]
            [amazonica.aws.servicediscovery       :as sd]
            [amazonica.aws.ec2                    :as ec2]
            [amazonica.aws.ecs                    :as ecs])
  (:import (com.amazonaws.services.ecs.model LogDriver NetworkMode AssignPublicIp)
           (com.amazonaws.services.cloudwatchevents.model LaunchType)))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- service-by-name [registry service-name]
  (->> (sd/list-services {:filters [{:name "NAMESPACE_ID"
                                     :values [registry]}]})
       (:services)
       (filter #(str/includes? (:name %) service-name))))


(defn- created? [service-registry service-name]
  (->>
     (service-by-name service-registry service-name)
     (empty?)
     (not)))

(defn- find-task-by-description [task description]
  (str/includes? (:description task) description))

(defn register-service-instance [sd-service {:keys [cluster-name ecs-task-definition]} active-task]
   (println (str "Register in service discovery service: " (:name sd-service)))
   (let [
         port (get-in ecs-task-definition [:container-definitions 0 :port-mappings 0 :host-port])
         net-id (get-in active-task [:attachments 0 :id])
         networks (ec2/describe-network-interfaces {})
         interface (first (filter #(find-task-by-description % net-id) (:network-interfaces networks)))
         ip-address (get-in interface [:private-ip-addresses 0 :private-ip-address])]
     (sd/register-instance {
                                  :creator-request-id (uuid)
                                  :instance-id (:name sd-service)
                                  :service-id (:id sd-service)
                                  :attributes {"type" "IP Address"
                                               "AWS_INSTANCE_IPV4" ip-address
                                               "AWS_INSTANCE_PORT" (str port)}})))

(defn upsert-service-discovery [current-service {:keys [service registry-namespace]}]
   (println (str "Configuring Service Discovery in " registry-namespace))
   (if (created? registry-namespace (:name service))
     (first (service-by-name registry-namespace (:name service)))
     (sd/create-service {
                               :creator-request-id (uuid)
                               :description (:name name)
                               :namespace-id registry-namespace
                               :name (:name service)
                               :dns-config {
                                            :routing-policy "MULTIVALUE"
                                            :dns-records [{
                                                           :ttl 60
                                                           :type "A"}
                                                          {:ttl 60
                                                           :type "SRV"}]}})))
