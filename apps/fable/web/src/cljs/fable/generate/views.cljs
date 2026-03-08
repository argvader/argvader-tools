(ns fable.generate.views
  (:require
   [re-frame.core :as re-frame]
   [fable.generate.subs]
   [fable.ui.components :as ui]
   [fable.ui.styles :as styles]))

(defn progress-bar [pct]
  [:div {:style {:background    (:bg-surface styles/palette)
                 :border-radius "8px"
                 :height        "12px"
                 :margin        "16px 0"
                 :overflow      "hidden"}}
   [:div {:style {:background  (:accent styles/palette)
                  :height      "100%"
                  :width       (str pct "%")
                  :transition  "width 0.5s ease"
                  :border-radius "8px"}}]])

(defn generate-view []
  (re-frame/dispatch [:generate/start])
  (fn []
    (let [progress @(re-frame/subscribe [:generate/progress])
          status   @(re-frame/subscribe [:generate/status])
          error    @(re-frame/subscribe [:generate/error])]
      [ui/page-frame
       [ui/step-indicator :generate]
       [ui/heading "Crafting Your Story"]
       [ui/subheading "GPT-4o is analyzing your moments and writing your storybook..."]
       [progress-bar progress]
       [:p {:style {:text-align "center"
                    :color      (:text-muted styles/palette)}}
        (case status
          "pending" "Initializing..."
          "running" (str progress "% — Writing your story")
          "done"    "Complete! Loading preview..."
          "error"   (str "Error: " error)
          "Waiting...")]
       (when (= "error" status)
         [ui/secondary-button "← Back" #(re-frame/dispatch [:wizard/back])])])))
