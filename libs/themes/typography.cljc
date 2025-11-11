(ns themes.typography
  (:refer-clojure :exclude [rem])
  (:require [garden.units :as units :refer (px pt pc em rem vw)]
            [stylefy.core :as stylefy]))


(def title (clojure.string/join ", " ["neue-haas-grotesk-display" "Comic Sans" "sans-serif"]))
(def basic (clojure.string/join ", " ["neue-haas-grotesk-text" "Avenir" "Helvetica" "sans-serif"]))
(def mono (clojure.string/join ", " ["Inconsolata" "Menlo" "Courier" "monospace"]))

(defn load-fonts []
  (stylefy/font-face {:font-family "neue-haas-grotesk-display"
                      :src "url('https://use.typekit.net/af/28f000/00000000000000003b9b2048/27/l?subset_id=2&fvd=n5&v=3') format('woff2'),url('https://use.typekit.net/af/28f000/00000000000000003b9b2048/27/d?subset_id=2&fvd=n5&v=3') format('woff'),url('https://use.typekit.net/af/28f000/00000000000000003b9b2048/27/a?subset_id=2&fvd=n5&v=3') format('opentype')"
                      :font-weight "500"
                      :font-style "normal"})


  (stylefy/font-face {:font-family "neue-haas-grotesk-display"
                      :src "url('https://use.typekit.net/af/d562ce/00000000000000003b9b204c/27/l?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n7&v=3') format('woff2'),url('https://use.typekit.net/af/d562ce/00000000000000003b9b204c/27/d?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n7&v=3') format('woff'),url('https://use.typekit.net/af/d562ce/00000000000000003b9b204c/27/a?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n7&v=3') format('opentype')"
                      :font-weight "700"
                      :font-style "normal"})


  (stylefy/font-face {:font-family "neue-haas-grotesk-text"
                      :src "url('https://use.typekit.net/af/1285d2/00000000000000003b9b2050/27/l?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n4&v=3') format('woff2'),url('https://use.typekit.net/af/1285d2/00000000000000003b9b2050/27/d?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n4&v=3') format('woff'),url('https://use.typekit.net/af/1285d2/00000000000000003b9b2050/27/a?primer=7cdcb44be4a7db8877ffa5c0007b8dd865b3bbc383831fe2ea177f62257a9191&fvd=n4&v=3') format('opentype')"
                      :font-weight "400"
                      :font-style "normal"})


  (stylefy/font-face {:font-family "neue-haas-grotesk-text"
                      :src "url('https://use.typekit.net/af/abbb5b/00000000000000003b9b2054/27/l?subset_id=2&fvd=n7&v=3') format('woff2'),url('https://use.typekit.net/af/abbb5b/00000000000000003b9b2054/27/d?subset_id=2&fvd=n7&v=3') format('woff'),url('https://use.typekit.net/af/abbb5b/00000000000000003b9b2054/27/a?subset_id=2&fvd=n7&v=3') format('opentype')"
                      :font-weight "700"
                      :font-style "normal"}))
