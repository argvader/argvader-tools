(ns fable.storybook.pdf
  (:require
   [fable.storybook.themes :as themes]
   [clj-http.client :as http])
  (:import
   [com.lowagie.text Document Paragraph Chapter Chunk Font Image PageSize]
   [com.lowagie.text.pdf PdfWriter]
   [java.io ByteArrayOutputStream]
   [java.net URL]
   [java.awt Color]))

(defn- theme-font [theme-key size style]
  (let [typo  (themes/typography theme-key)
        fname (:heading-font typo "Helvetica")]
    (Font. Font/HELVETICA (float size) style)))

(defn- fetch-image-bytes [url]
  (try
    (let [resp (http/get url {:as :byte-array :throw-exceptions false})]
      (when (= 200 (:status resp))
        (:body resp)))
    (catch Exception _ nil)))

(defn generate-pdf
  "Generate a PDF byte array from a storybook map."
  [storybook theme]
  (let [out  (ByteArrayOutputStream.)
        doc  (Document. PageSize/A4)]
    (PdfWriter/getInstance doc out)
    (.open doc)
    ;; Cover page
    (.add doc (Paragraph. (str (:title storybook))
                          (theme-font theme 28 Font/BOLD)))
    (.add doc (Paragraph. (str (:tagline storybook))
                          (theme-font theme 16 Font/ITALIC)))
    (.newPage doc)
    ;; Pages
    (doseq [page (:pages storybook)]
      (.add doc (Paragraph. (str "Chapter: " (:chapter page))
                            (theme-font theme 18 Font/BOLD)))
      (when-let [img-bytes (and (:image-url page)
                                (fetch-image-bytes (:image-url page)))]
        (let [img (Image/getInstance img-bytes)]
          (.scaleToFit img 450 300)
          (.add doc img)))
      (.add doc (Paragraph. (str (:body page))
                            (theme-font theme 12 Font/NORMAL)))
      (.newPage doc))
    (.close doc)
    (.toByteArray out)))
