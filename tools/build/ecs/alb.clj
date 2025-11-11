(ns build.ecs.alb
  (:require [clojure.string                         :as str]
            [amazonica.aws.elasticloadbalancingv2   :as lb]
            [build.ecs.route53                      :as rt]
            [amazonica.aws.ecs                      :as ecs]
            [amazonica.aws.ec2                      :as ec2]
            [build.aws                              :refer [build-tags]])
  (:import (com.amazonaws.services.elasticloadbalancingv2.model ProtocolEnum ActionTypeEnum)))

(defn- create-target-group [{:keys [service ecs-task-definition vpc-id tags]}]
  (let [target-group-name(str (:name service) "-tg")
        network (get-in ecs-task-definition [:container-definitions 0 :port-mappings 0])]
    (println "Create Target Group " target-group-name)
    (->> (lb/create-target-group
          {
           :health-check-enabled true
           :health-check-path "/health"
           :health-check-port (:container-port network)
           :health-check-protocol ProtocolEnum/HTTP
           :health-check-timeout-seconds 3
           :health-check-threshold-count 5
           :matcher {:http-code 200}
           :name target-group-name
           :port (:container-port network)
           :protocol ProtocolEnum/HTTP
           :target-type "ip"
           :unhealthy-threshold-count 10
           :healthy-threshold-count 2
           :vpc-id vpc-id})
         (:target-groups)
         (first)
         (assoc {} :target-group))))


(defn- add-target-group-tags [data {:keys [tags service]}]
  (let [target (:target-group data)]
    (lb/add-tags {:tags (build-tags tags (:target-group-name target)) :resource-arns (vector (:target-group-arn target))})
    data))

(defn- existing-listener [lb-arn]
  (try
   (:listeners (lb/describe-listeners {:load-balancer-arn lb-arn}))
   (catch Exception e
     [])))

(defn create-listener [data {:keys [tags service]}]
  (let [target-arn (get-in data [:target-group :target-group-arn])
        lb (:load-balancer data)
        existing-listener (existing-listener (:load-balancer-arn lb))]
    (if (empty? existing-listener)
      (->> (lb/create-listener
             {
              :load-balancer-arn (:load-balancer-arn lb)
              :certificates [{:certificate-arn "arn:aws:acm:us-east-1:245511257894:certificate/ca818a90-7cdb-422e-b4be-b6178462345c"}]
              :is-default true
              :port 443
              :protocol ProtocolEnum/HTTPS
              :ssl-policy "ELBSecurityPolicy-TLS-1-2-2017-01"
              :default-actions [{:type ActionTypeEnum/Forward
                                 :target-group-arn target-arn}]})
           (:listeners)
           (first)
           (assoc data :listener))
      (assoc data :listener (first existing-listener)))))


(defn- existing-lb [lb-name]
  (try
    (:load-balancers (lb/describe-load-balancers {:names [lb-name]}))
    (catch Exception e
      [])))

(defn- create-load-balancer [data {:keys [load-balancer tags service]}]
  (let [lb-name (:name load-balancer)
        lb-config (assoc load-balancer :tags (build-tags tags (:name load-balancer)))
        existing-lb (existing-lb lb-name)]
    (if (empty? existing-lb)
      (->> (lb/create-load-balancer lb-config)
           (:load-balancers)
           (first)
           (assoc data :load-balancer))
      (assoc data :load-balancer (first existing-lb)))))

(defn- rule-by-host [rule name]
  (reduce #(or %1 (= name (get-in %2 [:host-header-config :values 0]))) false (:conditions rule)))

(defn- upsert-rules [data service rules]
  (let [rule-host (str (:name service) "." (:domain service))
        priority (inc (count rules))
        this-rule (filter #(rule-by-host % rule-host) rules)]
    (when (empty? this-rule)
      (lb/create-rule
        {:listener-arn (get-in data [:listener :listener-arn])
         :priority priority
         :conditions [{:field "host-header"
                       :host-header-config {:values [rule-host]}}]
         :actions [{
                    :type ActionTypeEnum/Forward
                    :order 1
                    :forward-config {
                                     :target-groups [{:target-group-arn (get-in data [:target-group :target-group-arn])}]}}]}))
    data))

(defn- find-network-by-description [networks description]
  (->> networks
      (:network-interfaces)
      (filter #(str/includes? % description))
      (first)))

(defn- remove-old-targets [data {:keys [service]}]
  (let [target-groups (lb/describe-target-groups {:load-balancer-arn (get-in data [:load-balancer :load-balancer-arn])})]
    (for [target-group (:target-groups target-groups)]
       (lb/deregister-targets {:targets [{:target-group-arn target-group}]}))
    data))

(defn- create-network-interface [data {:keys [load-balancer tags]}]
  (println "Create Network Interface")
  (let [networks (ec2/describe-network-interfaces {})
        interface (find-network-by-description networks (:name load-balancer))]
    (if (nil? interface)
      (assoc data :interface (get (ec2/create-network-interface {:description (:name load-balancer)}
                                     :subnetId (get-in load-balancer [:network-interface :subnet-id])
                                     :group [(get-in load-balancer [:network-interface :security-group])])
                                  :network-interface)))
   (assoc data :interface interface)))

(defn- modify-rule [data {:keys [service]}]
  (println "Modifying Rule List")
  (let [listener-arn (get-in data [:listener :listener-arn])]
    (->> (lb/describe-rules {:set-page-size 50
                                   :listener-arn listener-arn})
         (:rules)
         (upsert-rules data service))))

(defn register-target [data {:keys [service ecs-task-definition cluster-name]}]
  (println "Register ALB Target")
  (let [interface (:interface data)]
    (lb/register-targets
      {
       :target-group-arn (get-in data [:target-group :target-group-arn])
       :targets [{
                  :id (get-in interface [:private-ip-addresses 0 :private-ip-address])
                  :port (get-in ecs-task-definition [:container-definitions 0 :port-mappings 0 :container-port])}]})
    data))

(defn upsert-load-balancer [config running-task]
  (println (str "Configuring ALB " (get-in config [:load-balancer :name])))
  (-> (create-target-group config)
      (assoc :active-task running-task)
      (add-target-group-tags config)
      (create-load-balancer config)
      (create-listener config)
      (modify-rule config)
      (rt/update-route config)
      (remove-old-targets config)
      (create-network-interface config)))
