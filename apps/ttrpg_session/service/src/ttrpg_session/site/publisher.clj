(ns ttrpg-session.site.publisher
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [env.config :as env]))

(defn- gh-cfg [] (get-in env/config [:github]))
(defn- branch [] (get (gh-cfg) :branch "main"))

(defn- api-url [path]
  (let [{:keys [owner repo]} (gh-cfg)]
    (str "https://api.github.com/repos/" owner "/" repo "/contents/" path)))

(defn- auth-headers []
  {"Authorization"        (str "Bearer " (:token (gh-cfg)))
   "Accept"               "application/vnd.github+json"
   "X-GitHub-Api-Version" "2022-11-28"})

(defn- b64-encode [^String s]
  (.encodeToString (java.util.Base64/getEncoder) (.getBytes s "UTF-8")))

(defn- b64-encode-bytes [^bytes b]
  (.encodeToString (java.util.Base64/getEncoder) b))

(defn- file-sha
  "Return the current blob SHA for path on the target branch, or nil if absent."
  [path]
  (try
    (let [resp (http/get (api-url path)
                         {:headers          (auth-headers)
                          :query-params     {"ref" (branch)}
                          :throw-exceptions false})]
      (when (= 200 (:status resp))
        (:sha (json/parse-string (:body resp) true))))
    (catch Exception _ nil)))

(defn- put-file!
  "Create or update a UTF-8 text file on the target branch."
  [path content]
  (let [sha  (file-sha path)
        body (cond-> {:message (str "publish " path)
                      :content (b64-encode content)
                      :branch  (branch)}
               sha (assoc :sha sha))]
    (http/put (api-url path)
              {:headers      (auth-headers)
               :body         (json/generate-string body)
               :content-type :json
               :accept       :json})))

(defn- put-binary-file!
  "Create or update a binary file on the target branch."
  [path ^bytes content-bytes]
  (let [sha  (file-sha path)
        body (cond-> {:message (str "publish " path)
                      :content (b64-encode-bytes content-bytes)
                      :branch  (branch)}
               sha (assoc :sha sha))]
    (http/put (api-url path)
              {:headers      (auth-headers)
               :body         (json/generate-string body)
               :content-type :json
               :accept       :json})))

(defn publish-image!
  "Upload PNG bytes to docs/images/{session-id}-scene.png. Returns the path."
  [session-id ^bytes png-bytes]
  (let [path (str "docs/images/" session-id "-scene.png")]
    (put-binary-file! path png-bytes)
    path))

(defn publish-files!
  "Publish a seq of {:path str :content str} text files to GitHub.
   Adds a short sleep between requests to respect GitHub secondary rate limits."
  [file-maps]
  (doseq [{:keys [path content]} file-maps]
    (put-file! path content)
    (Thread/sleep 500)))
