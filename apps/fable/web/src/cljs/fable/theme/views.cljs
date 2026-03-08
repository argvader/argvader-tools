(ns fable.theme.views
  (:require
   [re-frame.core :as re-frame]
   [fable.theme.subs]
   [fable.ui.components :as ui]
   [fable.ui.styles :as styles]))

(def themes
  [{:key :romantic-comedy
    :label "Romantic Comedy"
    :emoji "💕"
    :desc  "Witty, warm, and full of charming mishaps. Your life as a feel-good movie."}
   {:key :horror
    :label "Horror"
    :emoji "🕷️"
    :desc  "Find the dread in the everyday. A slow-burn tale of psychological tension."}
   {:key :sci-fi
    :label "Sci-Fi"
    :emoji "🚀"
    :desc  "Your journey as an epic across galaxies and timelines. Visionary and bold."}])

(defn theme-card [theme selected?]
  [:div {:on-click #(re-frame/dispatch [:theme/select (:key theme)])
         :style    {:cursor         "pointer"
                    :border-radius  "12px"
                    :padding        "20px"
                    :background     (if selected?
                                      (:bg-surface styles/palette)
                                      (:bg-card styles/palette))
                    :border         (if selected?
                                      (str "2px solid " (:accent styles/palette))
                                      "2px solid transparent")
                    :transition     "all 0.2s"
                    :margin-bottom  "12px"}}
   [:div {:style {:font-size "2rem" :margin-bottom "8px"}} (:emoji theme)]
   [:div {:style {:font-weight "700" :margin-bottom "4px"}} (:label theme)]
   [:div {:style {:color (:text-muted styles/palette) :font-size "0.9rem"}} (:desc theme)]])

(defn theme-view []
  (fn []
    (let [selected @(re-frame/subscribe [:theme/selected])]
      [ui/page-frame
       [ui/step-indicator :theme]
       [ui/heading "Choose Your Genre"]
       [ui/subheading "Pick the cinematic style for your storybook"]
       [:div {:style {:margin-bottom "24px"}}
        (for [theme themes]
          ^{:key (name (:key theme))}
          [theme-card theme (= (:key theme) selected)])]
       [:div {:style {:display "flex" :gap "12px"}}
        [ui/secondary-button "← Back" #(re-frame/dispatch [:wizard/back])]
        [ui/button "Generate My Story →"
         #(re-frame/dispatch [:theme/confirm])
         {:disabled (nil? selected)}]]])))
