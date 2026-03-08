(ns fable.meta.posts
  (:require
   [fable.meta.client :as client]
   [fable.meta.transform :as transform]))

(defn- fetch-page [access-token cursor]
  (client/graph-get "/me/media"
                    access-token
                    (cond-> {:fields "id,caption,media_url,timestamp,media_type"
                             :limit  50}
                      cursor (assoc :after cursor))))

(defn fetch-all-posts
  "Fetch all Instagram media posts with pagination. Returns normalized post maps."
  [access-token & {:keys [max-pages] :or {max-pages 5}}]
  (loop [cursor nil
         page   0
         acc    []]
    (if (>= page max-pages)
      acc
      (let [resp   (fetch-page access-token cursor)
            items  (get resp :data [])
            next   (get-in resp [:paging :cursors :after])
            posts  (mapv transform/normalize-post items)
            images (filter #(= "IMAGE" (:media-type %)) posts)]
        (if (or (empty? items) (nil? next))
          (into acc images)
          (recur next (inc page) (into acc images)))))))
