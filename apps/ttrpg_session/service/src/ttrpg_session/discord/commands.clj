(ns ttrpg-session.discord.commands
  (:require
   [ttrpg-session.db.store :as store]
   [ttrpg-session.discord.voice :as voice]
   [ttrpg-session.config.roster :as roster]
   [ttrpg-session.pipeline.processor :as processor])
  (:import
   [net.dv8tion.jda.api.hooks ListenerAdapter]
   [net.dv8tion.jda.api.events.guild GuildReadyEvent]
   [net.dv8tion.jda.api.events.interaction.command SlashCommandInteractionEvent]
   [net.dv8tion.jda.api.interactions.commands.build Commands SubcommandData]))

(def ^:private active-sessions
  "Map of guild-name -> {:session-id str :handler-map map}"
  (atom {}))

(defn- register-commands! [guild]
  (let [cmd (-> (Commands/slash "scribe" "Manage TTRPG session recording")
                (.addSubcommands
                 (into-array SubcommandData
                             [(SubcommandData. "start" "Start recording this session")
                              (SubcommandData. "stop"  "Stop recording and process")])))]
    (-> (.updateCommands guild)
        (.addCommands (into-array net.dv8tion.jda.api.interactions.commands.build.CommandData [cmd]))
        (.queue))))

(defn- handle-start! [^SlashCommandInteractionEvent event db]
  (let [member   (.getMember event)
        guild    (.getGuild event)
        vc-state (.getVoiceState member)]
    (if (and vc-state (.inAudioChannel vc-state))
      (let [channel       (.getChannel vc-state)
            channel-name  (.getName channel)
            guild-name    (.getName guild)
            campaign-name (roster/campaign-name)
            session       (store/create-recording-session!
                           {:discord-guild guild-name
                            :channel-name  channel-name
                            :campaign-name campaign-name})
            session-id    (str (:id session))
            handler-map   (voice/make-handler)
            audio-mgr     (.getAudioManager guild)]
        (swap! active-sessions assoc guild-name
               {:session-id  session-id
                :handler-map handler-map})
        (.setReceivingHandler audio-mgr (:jda-handler handler-map))
        (.openAudioConnection audio-mgr channel)
        (-> (.reply event (str "Recording started! Session: " session-id))
            (.setEphemeral true)
            .queue))
      (-> (.reply event "You must be in a voice channel to start recording.")
          (.setEphemeral true)
          .queue))))

(defn- handle-stop! [^SlashCommandInteractionEvent event db]
  (let [guild        (.getGuild event)
        guild-name   (.getName guild)
        session-info (get @active-sessions guild-name)]
    (if session-info
      (let [{:keys [session-id handler-map]} session-info
            audio-mgr (.getAudioManager guild)
            pcm-bufs  (voice/get-buffers handler-map)]
        (.closeAudioConnection audio-mgr)
        (swap! active-sessions dissoc guild-name)
        (store/end-recording-session! session-id)
        (-> (.reply event "Recording stopped. Processing session...")
            (.setEphemeral true)
            .queue)
        (future (processor/process-session! session-id pcm-bufs db)))
      (-> (.reply event "No active recording in this server.")
          (.setEphemeral true)
          .queue))))

(defn make-listener [db]
  (proxy [ListenerAdapter] []
    (onGuildReady [^GuildReadyEvent event]
      (register-commands! (.getGuild event)))
    (onSlashCommandInteraction [^SlashCommandInteractionEvent event]
      (when (= "scribe" (.getName event))
        (case (.getSubcommandName event)
          "start" (handle-start! event db)
          "stop"  (handle-stop!  event db)
          nil)))))
