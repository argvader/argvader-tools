(require '[aero.core :as aero]
         '[clojure.java.io :as io])

(let [config (aero/read-config (io/resource "config.edn") {:profile :dev})]
  {:store :database
   :db {:dbtype   (get-in config [:fable-db :dbtype])
        :dbname   (get-in config [:fable-db :dbname])
        :host     (get-in config [:fable-db :host])
        :port     (get-in config [:fable-db :port])
        :user     (get-in config [:fable-db :user])
        :password (get-in config [:fable-db :password])}})
