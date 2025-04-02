(ns build.cljs.shadow
  (:require [aero.core :as aero]
            [env.config :as env]
            [env.core :as env-core]
            [clojure.java.io :as io]
            [shadow.cljs.devtools.config :as shadow-config]
            [shadow.cljs.devtools.api :as shadow]))

(defn release
  "Build :browser release, with advanced compilation"
  ([] (release "prod"))
  ([release-flag]
   (shadow/with-runtime
       (let [build-config (-> (shadow-config/get-build! :app)
                              (assoc ::release-flag release-flag))]
         (shadow/release* build-config {})))))

(defn watch
  "Watch the :browser release, reloading on changes."
  {:shadow/requires-server true}
  ([] (watch "dev"))
  ([release-flag]
   (shadow/watch (-> (shadow-config/get-build! :app)
                     (assoc ::release-flag release-flag)))))

(defn load-env
  {:shadow.build/stages #{:compile-prepare}}
  [{:as build-state
    :keys [shadow.build/config]}]
  (let [app-env (env-core/set-env! (env-core/read-env (-> config ::release-flag keyword)))]
    (assoc-in build-state [:compiler-options :external-config ::env] app-env)))
