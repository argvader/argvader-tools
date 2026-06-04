(ns ttrpg-session.site.generator
  (:require [clojure.string :as str]))

;; --- formatting helpers ---

(defn- slugify [s]
  (-> (str/lower-case (str s))
      (str/replace #"[^a-z0-9\s-]" "")
      (str/replace #"\s+" "-")
      (str/replace #"-+" "-")
      str/trim))

(defn- ->utc-date-time
  "Coerce a JDBC timestamp value to an OffsetDateTime in UTC. timestamptz columns
   come back as java.sql.Timestamp (pgjdbc default), but may be java.time types
   depending on driver config — normalise so date formatting works regardless."
  ^java.time.OffsetDateTime [ts]
  (let [^java.time.Instant inst
        (condp instance? ts
          java.time.Instant        ts
          java.sql.Timestamp       (.toInstant ^java.sql.Timestamp ts)
          java.time.OffsetDateTime (.toInstant ^java.time.OffsetDateTime ts)
          java.time.ZonedDateTime  (.toInstant ^java.time.ZonedDateTime ts))]
    (.atOffset inst java.time.ZoneOffset/UTC)))

(defn date-slug
  "Format a JDBC timestamp value as YYYY-MM-DD (UTC)."
  [ts]
  (when ts
    (.format (->utc-date-time ts) java.time.format.DateTimeFormatter/ISO_LOCAL_DATE)))

(defn- format-date
  "Format a JDBC timestamp value as 'Month D, YYYY' (UTC)."
  [ts]
  (when ts
    (.format (->utc-date-time ts)
             (java.time.format.DateTimeFormatter/ofPattern
              "MMMM d, yyyy" java.util.Locale/ENGLISH))))

(defn- safe-cell
  "Escape pipe characters in a Markdown table cell."
  [s]
  (str/replace (str s) "|" "\\|"))

(defn- bullet-list [items]
  (when (seq items)
    (str/join "\n" (map #(str "- " %) items))))

(defn- numbered-list [items]
  (when (seq items)
    (str/join "\n" (map-indexed #(str (inc %1) ". " %2) items))))

(defn- table [headers rows]
  (let [header-row (str "| " (str/join " | " headers) " |")
        sep-row    (str "| " (str/join " | " (repeat (count headers) "---")) " |")
        data-rows  (map (fn [row]
                          (str "| " (str/join " | " (map safe-cell row)) " |"))
                        rows)]
    (str/join "\n" (concat [header-row sep-row] data-rows))))

(defn- section
  "Emit a ## section only when body is non-blank."
  [title body]
  (when (seq (str/trim (str body)))
    (str "## " title "\n\n" body "\n")))

(defn- subsection [title body]
  (when (seq (str/trim (str body)))
    (str "### " title "\n\n" body "\n")))

(defn- overview-paragraphs [text]
  (when text
    (str/join "\n\n"
              (map str/trim (str/split text #"\n\n+")))))

(defn- session-link
  "Markdown link from a wiki/* page to a session page."
  [session-number session-title date-slug]
  (let [label (str "Session " session-number
                   (when session-title (str " — " session-title)))]
    (str "[" label "](../sessions/" date-slug ".md)")))

;; --- session page ---

(defn session-page
  "Returns {:path str :content str} for the session Markdown page.
   session: recording_sessions row.
   summary: session_summaries row with parsed JSONB fields.
   session-number: 1-based integer."
  [session summary session-number]
  (let [ds       (date-slug (:started_at session))
        title    (or (:session_title summary) (str "Session " session-number))
        hed      (str "Session " session-number " — " title)
        img-path (when (:scene_image_path summary)
                   (str "../images/" (last (str/split (:scene_image_path summary) #"/"))))

        key-events-md
        (when (seq (:key_events_detail summary))
          (str/join "\n\n"
                    (map (fn [{:keys [title body]}]
                           (str "### " title "\n\n"
                                (str/join "\n\n"
                                          (map str/trim (str/split (str body) #"\n\n+")))))
                         (:key_events_detail summary))))

        timeline-md
        (when (seq (:timeline_entries summary))
          (table ["Time" "Event"]
                 (map (fn [{:keys [time event]}] [time event])
                      (:timeline_entries summary))))

        items-md
        (when (seq (:items summary))
          (table ["Item" "Held By" "Notes"]
                 (map (fn [{:keys [item holder detail]}] [item holder detail])
                      (:items summary))))

        pc-moments-md
        (when (seq (:pc_moments summary))
          (table ["Character" "Highlight"]
                 (map (fn [{:keys [character-name moment]}] [character-name moment])
                      (:pc_moments summary))))

        transcript-md
        (when (seq (:transcript summary))
          (str/join "\n\n"
                    (map (fn [{:keys [character-name text]}]
                           (str "**" (or character-name "Unknown") ":** " text))
                         (:transcript summary))))

        content
        (str/join "\n"
          (filter some?
            ["---"
             (str "title: \"" hed "\"")
             "---"
             ""
             (str "# " hed)
             ""
             (str "*" (or (:campaign_name session) "Campaign")
                  " · " (format-date (:started_at session)) "*")
             ""
             "---"
             ""
             (when (and (:scene_title summary) img-path)
               (str "## " (:scene_title summary) "\n\n"
                    "!["  (:scene_title summary) "](" img-path ")\n\n"
                    "> " (str/replace (str (:scene_prose summary)) "\n" "\n> ")
                    "\n\n---"))
             ""
             (section "Overview" (overview-paragraphs (:overview summary)))
             (section "Key Events" key-events-md)
             (section "Timeline" timeline-md)
             (section "Notable Items" items-md)
             (section "Lore Learned" (bullet-list (:lore_learned summary)))
             (section "Mysteries Raised" (bullet-list (:mysteries summary)))
             (section "Commitments Made" (bullet-list (:commitments summary)))
             (section "What Comes Next" (numbered-list (:next_steps summary)))
             (section "Memorable Moments" (bullet-list (:memorable_moments summary)))
             (section "Characters This Session" pc-moments-md)
             (section "Transcript" transcript-md)]))]
    {:path    (str "docs/sessions/" ds ".md")
     :content content}))

;; --- npc page ---

(defn npc-page
  "Returns {:path str :content str}.
   npc: npcs row. notes: seq of {:notes :session_title :session_number :started_at}."
  [npc notes]
  (let [notes-md
        (when (seq notes)
          (table ["Session" "Notes"]
                 (map (fn [n]
                        (let [snum  (:session_number n)
                              stitle (:session_title n)
                              ds     (date-slug (:started_at n))
                              link   (session-link snum stitle ds)]
                          [link (:notes n)]))
                      notes)))

        content
        (str/join "\n"
          (filter some?
            ["---"
             (str "title: \"" (:name npc) "\"")
             "---"
             ""
             (str "# " (:name npc))
             ""
             (:description npc)
             ""
             (section "Session Appearances" notes-md)]))]
    {:path    (str "docs/wiki/npcs/" (:slug npc) ".md")
     :content content}))

;; --- location page ---

(defn location-page
  "Returns {:path str :content str}.
   location: locations row. notes: seq of {:notes :session_title :session_number :started_at}."
  [location notes]
  (let [notes-md
        (when (seq notes)
          (table ["Session" "Notes"]
                 (map (fn [n]
                        (let [snum   (:session_number n)
                              stitle (:session_title n)
                              ds     (date-slug (:started_at n))
                              link   (session-link snum stitle ds)]
                          [link (:notes n)]))
                      notes)))

        content
        (str/join "\n"
          (filter some?
            ["---"
             (str "title: \"" (:name location) "\"")
             "---"
             ""
             (str "# " (:name location))
             ""
             (:description location)
             ""
             (section "Session Appearances" notes-md)]))]
    {:path    (str "docs/wiki/locations/" (:slug location) ".md")
     :content content}))

;; --- pc page ---

(defn pc-page
  "Returns {:path str :content str}.
   pc: {:discord-username str :character-name str}.
   moments: seq of {:moment :session-title :session-number :date-slug}."
  [pc moments]
  (let [moments-md
        (when (seq moments)
          (table ["Session" "Moment"]
                 (map (fn [m]
                        (let [link (session-link (:session-number m)
                                                 (:session-title m)
                                                 (:date-slug m))]
                          [link (:moment m)]))
                      moments)))

        content
        (str/join "\n"
          (filter some?
            ["---"
             (str "title: \"" (:character-name pc) "\"")
             "---"
             ""
             (str "# " (:character-name pc))
             ""
             (str "*Played by " (:discord-username pc) "*")
             ""
             (section "Session Moments" moments-md)]))]
    {:path    (str "docs/wiki/pcs/" (slugify (:character-name pc)) ".md")
     :content content}))

;; --- index page ---

(defn index-page
  "Returns {:path 'docs/index.md' :content str}.
   sessions: from list-sessions-with-titles."
  [campaign-name sessions]
  (let [done   (filter #(= "done" (:status %)) sessions)
        rows   (map (fn [s]
                      [(str (:session_number s))
                       (let [ds    (date-slug (:started_at s))
                             title (or (:session_title s) "Session")]
                         (str "[" title "](sessions/" ds ".md)"))
                       (format-date (:started_at s))])
                    done)
        tbl    (when (seq rows)
                 (table ["#" "Session" "Date"] rows))
        content
        (str/join "\n"
          ["---"
           "title: \"Home\""
           "---"
           ""
           (str "# " campaign-name)
           ""
           (str "Welcome to the session archive and campaign wiki for *" campaign-name "*.")
           ""
           (or tbl "*No sessions recorded yet.*")])]
    {:path    "docs/index.md"
     :content content}))

;; --- mkdocs.yml ---

(def ^:private default-theme
  {:scheme "slate" :primary "deep purple" :accent "purple"
   :text "Noto Serif" :code "Fira Mono"})

(def ^:private themes
  "Maps a roster :custom_theme alias to MkDocs Material palette/font settings.
   Colours are Material's named palette colours; fonts are Google Font names."
  {"desert" {:scheme "default" :primary "amber" :accent "deep orange"
             :text "Marcellus" :code "Fira Mono"}
   "egypt"  {:scheme "default" :primary "amber" :accent "brown"
             :text "Cinzel" :code "Fira Mono"}})

(defn- theme-config
  "Resolve a custom-theme alias to a Material theme map, falling back to default."
  [alias]
  (get themes alias default-theme))

(defn mkdocs-yml
  "Returns {:path 'mkdocs.yml' :content str}.
   sessions: from list-sessions-with-titles.
   npcs: from list-npcs.
   locations: from list-locations.
   pcs: seq of {:character-name :discord-username}.
   theme: optional :custom_theme alias from the roster (e.g. \"desert\")."
  [{:keys [campaign-name site-url sessions npcs locations pcs theme]}]
  (let [done-sessions (filter #(= "done" (:status %)) sessions)
        {:keys [scheme primary accent text code]} (theme-config theme)

        nav-sessions
        (str/join "\n"
          (map (fn [s]
                 (str "    - sessions/" (date-slug (:started_at s)) ".md"))
               done-sessions))

        nav-npcs
        (str/join "\n"
          (map (fn [n] (str "      - wiki/npcs/" (:slug n) ".md")) npcs))

        nav-locations
        (str/join "\n"
          (map (fn [l] (str "      - wiki/locations/" (:slug l) ".md")) locations))

        nav-pcs
        (str/join "\n"
          (map (fn [p]
                 (str "      - wiki/pcs/" (slugify (:character-name p)) ".md"))
               pcs))

        content
        (str/join "\n"
          (filter some?
            [(str "site_name: \"" campaign-name "\"")
             (when site-url (str "site_url: \"" site-url "\""))
             "theme:"
             "  name: material"
             "  palette:"
             (str "    - scheme: " scheme)
             (str "      primary: " primary)
             (str "      accent: " accent)
             "  font:"
             (str "    text: " text)
             (str "    code: " code)
             "  features:"
             "    - navigation.instant"
             "    - navigation.tracking"
             "    - navigation.sections"
             "    - navigation.expand"
             "    - navigation.top"
             "    - toc.follow"
             "    - search.highlight"
             ""
             "nav:"
             "  - Home: index.md"
             (when (seq done-sessions)
               (str "  - Sessions:\n" nav-sessions))
             "  - Wiki:"
             (when (seq pcs)
               (str "    - Player Characters:\n" nav-pcs))
             (when (seq npcs)
               (str "    - NPCs:\n" nav-npcs))
             (when (seq locations)
               (str "    - Locations:\n" nav-locations))]))]
    {:path    "mkdocs.yml"
     :content content}))

;; --- GitHub Actions workflow ---

(defn github-workflow
  "Returns {:path '.github/workflows/deploy-docs.yml' :content str}."
  []
  {:path    ".github/workflows/deploy-docs.yml"
   :content (str/join "\n"
              ["name: Deploy MkDocs"
               "on:"
               "  push:"
               "    branches: [main]"
               "  workflow_dispatch:"
               "permissions:"
               "  contents: write"
               "jobs:"
               "  deploy:"
               "    runs-on: ubuntu-latest"
               "    steps:"
               "      - uses: actions/checkout@v4"
               "        with:"
               "          fetch-depth: 0"
               "      - uses: actions/setup-python@v5"
               "        with:"
               "          python-version: \"3.x\""
               "      - run: pip install mkdocs-material"
               "      - run: mkdocs gh-deploy --force"])})
