(ns fable.sw
  (:require
   ["workbox-routing" :refer [registerRoute]]
   ["workbox-strategies" :refer [NetworkFirst CacheFirst]]
   ["workbox-precaching" :refer [precacheAndRoute]]))

(defn init []
  ;; Precache app shell files (populated by Workbox at build time)
  (precacheAndRoute (clj->js []))

  ;; Network-first for HTML and JS (always fresh)
  (registerRoute
   (fn [^js {:keys [request]}]
     (contains? #{"document" "script"} (.-destination request)))
   (NetworkFirst. #js {:cacheName "app-shell"}))

  ;; Cache-first for images
  (registerRoute
   (fn [^js {:keys [request]}]
     (= "image" (.-destination request)))
   (CacheFirst. #js {:cacheName "images"})))
