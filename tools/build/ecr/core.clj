(ns build.ecr.core
  (:require [env.core                             :as env-config]
            [env.config                           :as env]
            [clojure.java.io                      :as io]
            [clojure.string                       :as str]
            [clj-docker-client.core               :as docker]
            [clj-docker-client.requests           :as req]
            [build.aws                            :refer [build-tags set-profile! get-config]]
            [clojure.java.shell                   :as shell]
            [amazonica.aws.ecr                    :as ecr]))

(defn- decode-base64 [to-decode]
  (String. (.decode (java.util.Base64/getDecoder) to-decode)))

(defn- encode [to-encode]
  (.encode (java.util.Base64/getEncoder) (.getBytes to-encode)))

(defn- get-name [config]
  (get-in config [:service :name]))

(defn- get-version [config]
  (or (get-in config [:service :version]) "LATEST"))

(defn- find-login [data]
  (-> data
      (get :authorization-token)
      (decode-base64)
      (str/split #":")))

(defn- create-repository-idempotent [data config]
  (let [name (get-name config)
        tags (build-tags (:tags config))]
    (try
      (ecr/create-repository
        {:repository-name name
         :tags tags})
      (catch Exception e)
      (finally data)))
  data)

(defn- find-repository [data]
  (get data :proxy-endpoint))

(defn- docker-image-name [config]
  (let [image (get-name config)
        version (get-version config)
        account (:account config)]
    (str account ".dkr.ecr.us-east-1.amazonaws.com/" image ":" version)))

(defn- build-image [config]
  (let [name (docker-image-name config)]
    (shell/sh "docker" "build" "." "-t" name)))

;docker login --username AWS --password-stdin 245511257894.dkr.ecr.us-east-1.amazonaws.com
;docker push 245511257894.dkr.ecr.us-east-1.amazonaws.com/team-tree-api:latest

(defn- push-emr [data config]
  (let [account (:account config)
        login-creds (find-login data)
        repository (find-repository data)]
    (shell/sh "docker" "login" "--username" (get login-creds 0) "--password" (get login-creds 1) (str account ".dkr.ecr.us-east-1.amazonaws.com"))
    (shell/sh "docker" "push" (docker-image-name config))
    data))


(defn -main [& args]
   (let [config (get-config)]
     (set-profile!)
     (build-image config)
    (->
       (ecr/get-authorization-token {})
       (get :authorization-data)
       (first)
       (create-repository-idempotent config)
       (push-emr config))))
