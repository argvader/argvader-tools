(ns code-churn.tester
  (:require [com.stuartsierra.component :as component]
            [code-churn.gather.github :as github]))


(defrecord Tester []
  component/Lifecycle

  (start [this]
    (println "Testing")
    (github/process-all-data "Soluto-Private"))
  (stop [this]
    (println "Test shutting down")))


(defn new-test []
  {:tester (map->Tester {})})
