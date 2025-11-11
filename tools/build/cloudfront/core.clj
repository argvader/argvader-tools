(ns build.cloudfront.core
  (:require [clojure.string                       :as str]
            [amazonica.aws.cloudfront             :as cf]
            [build.aws                            :refer [set-profile!]]))
(defn- args?
  [args] (when (seq args) args))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn -main [& args]
   (let [id (first args)
         uuid (uuid)]
     (set-profile!)
     (cf/create-invalidation :distribution-id id
                             :invalidation-batch {
                                                  :caller-reference uuid
                                                  :paths {
                                                          :items ["/*"]
                                                          :quantity 1}})))
