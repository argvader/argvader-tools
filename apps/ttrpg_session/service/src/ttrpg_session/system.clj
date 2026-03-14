(ns ttrpg-session.system
  (:require
   [com.stuartsierra.component :as component]
   [ttrpg-session.db.store :as db]
   [ttrpg-session.discord.bot :as discord]
   [ttrpg-session.server :as server]))

(defn new-system []
  (merge
   (component/system-map)
   (db/new-connection)
   (discord/new-bot)
   (server/new-server)))
