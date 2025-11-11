(ns migrations.core
  (:require [migratus.core :as migratus]))

(def config {:store                :database
             :migration-dir        "resources/migrations/"
             :init-script          "init.sql" ;script should be located in the :migration-dir path
             ;defaults to true, some databases do not support
             ;schema initialization in a transaction
             :init-in-transaction? false
             :migration-table-name "foo_bar"
             :db {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname     "site.db"}})

;initialize the database using the 'init.sql' script
(migratus/init config)
