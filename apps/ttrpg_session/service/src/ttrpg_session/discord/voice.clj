(ns ttrpg-session.discord.voice
  (:import
   [net.dv8tion.jda.api.audio AudioReceiveHandler UserAudio]))

(defn make-handler
  "Returns a map with :jda-handler (AudioReceiveHandler instance)
   and :buffers (atom of {user-id -> {:username str :pcm-bufs [byte[]]}})."
  []
  (let [buffers (atom {})]
    {:jda-handler
     (proxy [AudioReceiveHandler] []
       (canReceiveUser [] true)
       (handleUserAudio [^UserAudio audio]
         (let [user     (.getUser audio)
               user-id  (.getId user)
               username (.getName user)
               pcm-data (.getAudioData audio 1.0)]
           (swap! buffers (fn [b]
                            (-> b
                                (assoc-in [user-id :username] username)
                                (update-in [user-id :pcm-bufs] (fnil conj []) pcm-data)))))))
     :buffers buffers}))

(defn get-buffers
  "Returns the current snapshot of all per-user PCM buffers."
  [{:keys [buffers]}]
  @buffers)
