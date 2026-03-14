(ns ttrpg-session.discord.bot
  (:require
   [com.stuartsierra.component :as component]
   [env.config :as env]
   [ttrpg-session.discord.commands :as commands])
  (:import
   [net.dv8tion.jda.api JDABuilder]
   [net.dv8tion.jda.api.requests GatewayIntent]
   [java.util EnumSet]))

(defrecord DiscordBot [ttrpg-db jda]
  component/Lifecycle

  (start [this]
    (println "Starting Discord bot")
    (let [token    (get-in env/config [:discord :token])
          intents  (EnumSet/of GatewayIntent/GUILD_VOICE_STATES
                               GatewayIntent/GUILD_MESSAGES)
          listener (commands/make-listener ttrpg-db)
          jda      (-> (JDABuilder/createDefault token)
                       (.enableIntents intents)
                       (.addEventListeners (into-array Object [listener]))
                       (.build)
                       (.awaitReady))]
      (assoc this :jda jda)))

  (stop [this]
    (when jda
      (.shutdown jda))
    (assoc this :jda nil)))

(defn new-bot []
  {:discord-bot (component/using (map->DiscordBot {}) [:ttrpg-db])})
