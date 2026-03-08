(ns fable.auth.views
  (:require
   [re-frame.core :as re-frame]
   [fable.auth.subs]
   [fable.ui.components :as ui]))

(defn connect-view []
  (re-frame/dispatch [:auth/check-status])
  (fn []
    (let [connected? @(re-frame/subscribe [:auth/token-present?])
          user       @(re-frame/subscribe [:auth/user])]
      [ui/page-frame
       [ui/step-indicator :connect]
       [ui/heading "Welcome to Fable"]
       [ui/subheading "Transform your social media into a cinematic storybook"]
       (if connected?
         [:div
          [:p {:style {:margin "0 0 16px"}}
           (str "Connected as " (:name user))]
          [ui/button "Continue →" #(re-frame/dispatch [:wizard/advance])]]
         [:div
          [ui/button "Connect Facebook / Instagram"
           #(re-frame/dispatch [:auth/connect-meta])]])])))

(defn connect-success-view []
  (re-frame/dispatch [:auth/callback-success])
  (re-frame/dispatch [:wizard/advance])
  (fn []
    [ui/page-frame
     [ui/heading "Connected!"]
     [ui/subheading "Redirecting you to the next step..."]]))
