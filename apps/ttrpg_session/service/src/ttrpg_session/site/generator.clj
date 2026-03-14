(ns ttrpg-session.site.generator
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5]]
   [clojure.string :as str]))

(defn- css []
  "body { font-family: Georgia, serif; max-width: 800px; margin: 0 auto; padding: 2em; color: #333; background: #fafaf8; }
   h1, h2, h3 { color: #1a1a2e; }
   a { color: #6c63ff; }
   .meta { color: #888; font-size: 0.9em; margin-bottom: 2em; }
   .narrative p { line-height: 1.8; margin: 0 0 1em; }
   .key-events li { margin: 0.4em 0; line-height: 1.6; }
   .quote { border-left: 3px solid #6c63ff; padding: 0.3em 1em; margin: 0.8em 0; }
   .quote .char { font-weight: bold; color: #6c63ff; display: block; margin-bottom: 0.2em; }
   details { margin: 1.5em 0; border: 1px solid #e0e0e0; border-radius: 4px; padding: 0.5em 1em; }
   summary { cursor: pointer; font-weight: bold; padding: 0.3em 0; }
   .transcript-line { margin: 0.3em 0; line-height: 1.5; }
   .ts { color: #aaa; font-size: 0.8em; font-family: monospace; margin-right: 0.5em; }
   .speaker { font-weight: bold; margin-right: 0.3em; }")

(defn- format-time [secs]
  (let [m (int (/ secs 60))
        s (int (mod secs 60))]
    (format "%d:%02d" m s)))

(defn session-page
  "Render a single session page. summary has :narrative :key-events :notable-quotes.
   transcript is [{:start :character-name :text}]."
  [session summary transcript]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title (str (or (:campaign_name session) "Session") " — " (:channel_name session))]
    [:style (css)]]
   [:body
    [:p [:a {:href "/"} "← Archive"]]
    [:h1 (or (:campaign_name session) "Session Summary")]
    [:p.meta
     (str (:channel_name session) " · " (:started_at session))]

    [:h2 "What Happened"]
    [:div.narrative
     (for [para (str/split (:narrative summary) #"\n\n+")]
       [:p para])]

    [:h2 "Key Events"]
    [:ul.key-events
     (for [evt (:key-events summary)]
       [:li evt])]

    [:h2 "Notable Quotes"]
    (for [{:keys [character quote]} (:notable-quotes summary)]
      [:div.quote
       [:span.char character]
       [:span (str "\"" quote "\"")]])

    [:details
     [:summary "Full Transcript"]
     [:div
      (for [{:keys [start character-name text]} transcript]
        [:div.transcript-line
         [:span.ts (format-time start)]
         [:span.speaker (str character-name ":")]
         text])]]]))

(defn index-page
  "Render the session archive index page."
  [sessions]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Session Archive"]
    [:style (css)]]
   [:body
    [:h1 "Session Archive"]
    (if (empty? sessions)
      [:p "No sessions yet."]
      [:ul
       (for [s sessions]
         [:li
          (if (= "done" (:status s))
            [:a {:href (str "/sessions/" (:id s) "/")}
             (str (or (:campaign_name s) "Session") " — " (:channel_name s)
                  " (" (:started_at s) ")")]
            [:span
             (str (or (:campaign_name s) "Session") " — " (:channel_name s)
                  " [" (:status s) "]")])])])]))
