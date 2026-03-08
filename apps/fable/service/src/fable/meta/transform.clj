(ns fable.meta.transform)

(defn normalize-post
  "Normalize a raw Meta Graph API media object to a consistent map."
  [raw]
  {:id         (:id raw)
   :caption    (or (:caption raw) "")
   :image-url  (or (:media_url raw) "")
   :timestamp  (or (:timestamp raw) "")
   :media-type (or (:media_type raw) "IMAGE")})
