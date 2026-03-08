(ns fable.export.views
  (:require
   [re-frame.core :as re-frame]
   [stylefy.core :refer [use-style]]
   [fable.export.subs]
   [fable.ui.components :as ui]
   [fable.ui.styles :as styles]))

(defn field [label value on-change]
  [:div {:style {:margin-bottom "16px"}}
   [:label {:style {:display     "block"
                    :font-size   "0.85rem"
                    :color       (:text-muted styles/palette)
                    :margin-bottom "4px"}}
    label]
   [:input {:value     (or value "")
            :on-change #(on-change (.. % -target -value))
            :style     {:width         "100%"
                        :padding       "10px"
                        :background    (:bg-surface styles/palette)
                        :border        "1px solid #444"
                        :border-radius "6px"
                        :color         (:text-light styles/palette)
                        :font-size     "1rem"}}]])

(defn export-view []
  (fn []
    (let [pdf-url     @(re-frame/subscribe [:export/pdf-url])
          print-order @(re-frame/subscribe [:export/print-order])
          form        @(re-frame/subscribe [:export/print-form])
          pdf-err     @(re-frame/subscribe [:export/pdf-error])
          print-err   @(re-frame/subscribe [:export/print-error])]
      [ui/page-frame
       [ui/step-indicator :export]
       [ui/heading "Your Fable is Ready"]
       [ui/subheading "Download as PDF or order a printed book delivered to you"]

       ;; PDF section
       [:div (merge (use-style styles/card-style) {:margin-bottom "24px"})
        [:h3 {:style {:margin "0 0 12px"}} "📄 Download PDF"]
        [ui/button "Download PDF" #(re-frame/dispatch [:export/download-pdf])]
        (when pdf-url
          [:a {:href pdf-url :target "_blank"
               :style {:display "block" :margin-top "8px" :color (:accent-alt styles/palette)}}
           "Click here if download didn't start"])
        (when pdf-err
          [:p {:style {:color (:error styles/palette)}} "PDF error. Please try again."])]

       ;; Print section
       [:div (use-style styles/card-style)
        [:h3 {:style {:margin "0 0 16px"}} "📚 Order a Printed Book"]
        [field "Full Name"
         (:name form)
         #(re-frame/dispatch [:export/update-print-form :name %])]
        [field "Street Address"
         (:street form)
         #(re-frame/dispatch [:export/update-print-form :street %])]
        [field "City"
         (:city form)
         #(re-frame/dispatch [:export/update-print-form :city %])]
        [field "State/Province"
         (:state form)
         #(re-frame/dispatch [:export/update-print-form :state %])]
        [field "Postal Code"
         (:postal-code form)
         #(re-frame/dispatch [:export/update-print-form :postal-code %])]
        [field "Country (2-letter code)"
         (:country form)
         #(re-frame/dispatch [:export/update-print-form :country %])]
        (when (:lulu-job-id print-order)
          [:p {:style {:color (:success styles/palette) :margin "12px 0"}}
           (str "Order placed! Job ID: " (:lulu-job-id print-order))])
        (when print-err
          [:p {:style {:color (:error styles/palette) :margin "8px 0"}} "Order failed. Please try again."])
        [ui/button "Order Printed Book ($29.99)"
         #(re-frame/dispatch [:export/submit-print
                              {:quantity 1
                               :shipping-address
                               {:name        (:name form)
                                :street1     (:street form)
                                :city        (:city form)
                                :state_code  (:state form)
                                :postcode    (:postal-code form)
                                :country_code (:country form)}}])]]

       [:div {:style {:margin-top "24px"}}
        [ui/secondary-button "← Back to Preview" #(re-frame/dispatch [:wizard/back])]]])))
