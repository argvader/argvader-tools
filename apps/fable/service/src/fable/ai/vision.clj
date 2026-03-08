(ns fable.ai.vision
  (:require
   [fable.ai.client :as ai]
   [fable.storybook.themes :as themes]))

(defn describe-image
  "Use GPT-4o Vision to describe an image in the context of a story theme."
  [image-url theme]
  (let [theme-name (themes/theme-label theme)
        prompt     (str "You are a creative writer for a " theme-name " storybook. "
                        "Describe this image as a vivid scene in 2-3 sentences, "
                        "matching the tone and tropes of the " theme-name " genre. "
                        "Focus on atmosphere, emotion, and narrative potential.")
        messages   [{:role    "user"
                     :content [{:type      "text"
                                :text      prompt}
                               {:type      "image_url"
                                :image_url {:url    image-url
                                            :detail "low"}}]}]
        response   (ai/chat-completion messages)]
    (ai/extract-text response)))
