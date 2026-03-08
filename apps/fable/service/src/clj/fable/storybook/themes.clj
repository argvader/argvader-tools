(ns fable.storybook.themes)

(def themes
  {:romantic-comedy {:label      "Romantic Comedy"
                     :typography {:heading-font "Georgia"
                                  :body-font    "Palatino"
                                  :accent-color "#e91e8c"}
                     :system-prompt
                     "You are a witty, warm romantic comedy screenwriter. Transform the user's life
story into a charming rom-com. Use humor, heart, and classic tropes: misunderstandings,
grand gestures, quirky best friends. Keep chapters light, funny, and emotionally resonant.
Always write in present tense. Each page should feel like a scene from a beloved movie."}

   :horror         {:label      "Horror"
                    :typography {:heading-font "Courier New"
                                 :body-font    "Garamond"
                                 :accent-color "#8b0000"}
                    :system-prompt
                    "You are a master of psychological horror in the tradition of Stephen King.
Transform the user's life story into a creeping, atmospheric horror tale. Find the dread
in the mundane. Build tension slowly. Use vivid, unsettling imagery. Each page should
leave the reader wanting to look over their shoulder."}

   :sci-fi         {:label      "Sci-Fi"
                    :typography {:heading-font "Roboto Mono"
                                 :body-font    "Open Sans"
                                 :accent-color "#00bcd4"}
                    :system-prompt
                    "You are a visionary science fiction author in the tradition of Ursula K. Le Guin.
Transform the user's life story into an epic science fiction narrative set in a richly
imagined future or alternate universe. Find metaphors in technology, space, time, and
humanity's potential. Each page should expand the imagination."}})

(defn theme-label [theme-key]
  (get-in themes [theme-key :label] (name theme-key)))

(defn system-prompt [theme-key]
  (get-in themes [theme-key :system-prompt] ""))

(defn typography [theme-key]
  (get-in themes [theme-key :typography] {}))
