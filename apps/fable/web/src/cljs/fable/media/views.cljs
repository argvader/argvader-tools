(ns fable.media.views
  (:require
   [re-frame.core :as re-frame]
   [fable.media.subs]
   [fable.ui.components :as ui]
   [fable.ui.styles :as styles]))

(defn post-card [post selected?]
  [:div {:on-click #(re-frame/dispatch [:media/toggle-selection (:id post)])
         :style    {:position       "relative"
                    :cursor         "pointer"
                    :border-radius  "8px"
                    :overflow       "hidden"
                    :border         (if selected?
                                      (str "3px solid " (:accent styles/palette))
                                      "3px solid transparent")
                    :transition     "border 0.2s"}}
   [:img {:src   (:image-url post)
          :alt   (:caption post)
          :style {:width   "100%"
                  :display "block"
                  :aspect-ratio "1"
                  :object-fit "cover"}}]
   (when selected?
     [:div {:style {:position   "absolute"
                    :top        "8px"
                    :right      "8px"
                    :background (:accent styles/palette)
                    :color      "#fff"
                    :border-radius "50%"
                    :width      "24px"
                    :height     "24px"
                    :display    "flex"
                    :align-items "center"
                    :justify-content "center"
                    :font-size  "14px"}}
      "✓"])])

(defn media-view []
  (re-frame/dispatch [:media/fetch])
  (fn []
    (let [posts    @(re-frame/subscribe [:media/posts])
          sel-ids  @(re-frame/subscribe [:media/selected-ids])]
      [ui/page-frame
       [ui/step-indicator :media]
       [ui/heading "Select Your Moments"]
       [ui/subheading (str "Choose up to 20 photos to include in your story ("
                           (count sel-ids) " selected)")]
       (if (empty? posts)
         [ui/spinner]
         [:div {:style {:display               "grid"
                        :grid-template-columns "repeat(3, 1fr)"
                        :gap                   "8px"
                        :margin-bottom         "24px"}}
          (for [post posts]
            ^{:key (:id post)}
            [post-card post (contains? sel-ids (:id post))])])
       [:div {:style {:display "flex" :gap "12px"}}
        [ui/secondary-button "← Back" #(re-frame/dispatch [:wizard/back])]
        [ui/button "Next →" #(re-frame/dispatch [:wizard/advance])
         {:disabled (empty? sel-ids)}]]])))
