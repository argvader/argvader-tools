(ns build.ecs.cluster
  (:require [clojure.string                       :as str]
            [amazonica.aws.ecs                    :as ecs]
            [build.aws                            :refer [build-tags]]))

(defn cluster-by-name [cluster-name]
  (->> (ecs/list-clusters)
       (:cluster-arns)
       (filter #(str/includes? % cluster-name))))

(defn created? [{:keys [cluster-name]}]
  (->>
     (cluster-by-name cluster-name)
     (empty?)
     (not)))

(defn upsert-cluster [{:keys [cluster-name tags]}]
  (println (str "Configuring Cluster " cluster-name))
  (if (created? {:cluster-name cluster-name})
    (cluster-by-name cluster-name)
    (ecs/create-cluster {:cluster-name cluster-name
                         :tags (build-tags tags)})))
