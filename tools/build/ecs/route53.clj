(ns build.ecs.route53
  (:require [amazonica.aws.route53                  :as rt]
            [amazonica.aws.elasticloadbalancingv2   :as lb])
  (:import (com.amazonaws.services.route53.model ChangeAction RRType)))


(defn update-route [data {:keys [zone-id service]}]
  (println "Create route53")
  (let [alb (:load-balancer data)
        service-host (str (:name service) "." (:domain service))]
    (rt/change-resource-record-sets 
      {:hosted-zone-id zone-id
       :change-batch
         {:changes [
                     {:action ChangeAction/UPSERT
                      :resource-recordset {
                                            :ttl 30
                                            :name service-host
                                            :type RRType/CNAME
                                            :resource-records [{
                                                                :value (:dnsname alb)}]}}]}}))
  data)
