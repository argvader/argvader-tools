(ns ttrpg-session.discord.bot
  (:require
   [com.stuartsierra.component :as component]
   [env.config :as env]
   [ttrpg-session.discord.commands :as commands])
  (:import
   [net.dv8tion.jda.api JDABuilder]
   [net.dv8tion.jda.api.requests GatewayIntent]
   [net.dv8tion.jda.api.audio AudioModuleConfig]
   [moe.kyokobot.libdave NativeDaveFactory]
   [moe.kyokobot.libdave.jda LDJDADaveSessionFactory]
   [java.util EnumSet]))

(defn- dave-audio-config
  "AudioModuleConfig wired with libdave-jvm's native DAVE (A/V E2EE) implementation.
   Discord requires DAVE on non-stage voice connections as of 2026-03-01.
   `ensureAvailable` fails fast with a clear error if the native lib can't load
   on this platform/arch."
  ^AudioModuleConfig []
  (NativeDaveFactory/ensureAvailable)
  (-> (AudioModuleConfig.)
      (.withDaveSessionFactory (LDJDADaveSessionFactory. (NativeDaveFactory.)))))

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
                       (.setAudioModuleConfig (dave-audio-config))
                       (.addEventListeners (into-array Object [listener]))
                       (.build)
                       (.awaitReady))]
      (assoc this :jda jda)))

  (stop [this]
    (when jda
      (.shutdown ^net.dv8tion.jda.api.JDA jda))
    (assoc this :jda nil)))

(defn new-bot []
  {:discord-bot (component/using (map->DiscordBot {}) [:ttrpg-db])})
