(ns fable.preview.views
  (:require
   [re-frame.core :as re-frame]
   [fable.preview.subs]
   [fable.ui.components :as ui]
   [fable.ui.styles :as styles]))

(defn page-view [page]
  [:div {:style {:min-height "300px"}}
   (when (:image-url page)
     [:img {:src   (:image-url page)
            :alt   (:chapter page)
            :style {:width         "100%"
                    :max-height    "300px"
                    :object-fit    "cover"
                    :border-radius "8px"
                    :margin-bottom "16px"}}])
   [:h2 {:style {:font-size   "1.3rem"
                 :font-weight "700"
                 :margin      "0 0 12px"}}
    (:chapter page)]
   [:p {:style {:line-height "1.7"
                :color       (:text-light styles/palette)}}
    (:body page)]])

(defn preview-view []
  (fn []
    (let [storybook @(re-frame/subscribe [:storybook/current])
          page-idx  @(re-frame/subscribe [:preview/page-index])
          page      @(re-frame/subscribe [:preview/current-page])
          total     (count (:pages storybook []))]
      [ui/page-frame
       [ui/step-indicator :preview]
       [ui/heading (or (:title storybook) "Your Fable")]
       [:p {:style {:color       (:text-muted styles/palette)
                    :font-style  "italic"
                    :margin      "0 0 24px"}}
        (:tagline storybook)]
       (when page [page-view page])
       [:div {:style {:display         "flex"
                      :justify-content "space-between"
                      :align-items     "center"
                      :margin-top      "24px"}}
        [ui/secondary-button "← Prev" #(re-frame/dispatch [:preview/prev-page])]
        [:span {:style {:color (:text-muted styles/palette)}}
         (str "Page " (inc page-idx) " of " total)]
        [ui/secondary-button "Next →" #(re-frame/dispatch [:preview/next-page])]]
       [:div {:style {:margin-top "24px" :display "flex" :gap "12px"}}
        [ui/secondary-button "← Back" #(re-frame/dispatch [:wizard/back])]
        [ui/button "Download / Print →" #(re-frame/dispatch [:wizard/advance])]]])))
