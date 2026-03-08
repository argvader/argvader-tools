(ns fable.ai.story
  (:require
   [fable.ai.client :as ai]
   [fable.storybook.themes :as themes]
   [cheshire.core :as json]))

(defn- format-posts-for-prompt [posts scene-descriptions]
  (mapv (fn [post scene]
          {:caption    (:caption post)
           :timestamp  (:timestamp post)
           :image-url  (:image-url post)
           :scene-desc scene})
        posts
        scene-descriptions))

(defn generate-storybook
  "Generate a full storybook JSON from posts and scene descriptions for a given theme."
  [posts scene-descriptions theme]
  (let [sys-prompt (themes/system-prompt theme)
        theme-name (themes/theme-label theme)
        formatted  (format-posts-for-prompt posts scene-descriptions)
        user-msg   (str "Here are the life moments to transform into a " theme-name " storybook:\n\n"
                        (json/generate-string formatted {:pretty true})
                        "\n\nGenerate a complete storybook as JSON with this structure:\n"
                        "{\n"
                        "  \"title\": \"<evocative title>\",\n"
                        "  \"tagline\": \"<one-line tagline>\",\n"
                        "  \"pages\": [\n"
                        "    {\n"
                        "      \"page-num\": 1,\n"
                        "      \"chapter\": \"<chapter title>\",\n"
                        "      \"body\": \"<3-4 paragraph story text>\",\n"
                        "      \"image-url\": \"<original image url>\",\n"
                        "      \"scene-desc\": \"<scene description>\"\n"
                        "    }\n"
                        "  ]\n"
                        "}")
        messages   [{:role "system" :content sys-prompt}
                    {:role "user"   :content user-msg}]
        response   (ai/chat-completion messages :json-mode? true)
        text       (ai/extract-text response)]
    (json/parse-string text true)))
