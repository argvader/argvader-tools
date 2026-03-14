(ns ttrpg-session.handlers.health)

(defn health-handler [_request]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "ttrpg-session OK"})
