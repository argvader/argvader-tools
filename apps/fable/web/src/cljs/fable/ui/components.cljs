(ns fable.ui.components
  (:require
   [stylefy.core :as stylefy :refer [use-style]]
   [fable.ui.styles :as styles]))

(def steps [:connect :media :theme :generate :preview :export])

(defn step-index [step]
  (.indexOf (clj->js steps) step))

(defn step-indicator [current-step]
  [:div (use-style styles/step-indicator-style)
   (map-indexed
    (fn [i step]
      (let [active?    (= step current-step)
            completed? (< (step-index step) (step-index current-step))]
        [:div {:key   (name step)
               :style {:width         "10px"
                       :height        "10px"
                       :border-radius "50%"
                       :background    (cond
                                        active?    (:accent styles/palette)
                                        completed? (:accent-alt styles/palette)
                                        :else      (:bg-surface styles/palette))
                       :transition    "background 0.3s"}}]))
    steps)])

(defn page-frame [& children]
  [:div {:style {:min-height      "100vh"
                 :display         "flex"
                 :flex-direction  "column"
                 :align-items     "center"
                 :justify-content "center"
                 :padding         "24px"
                 :background      (:bg-deep styles/palette)}}
   (into [:div {:style (merge styles/card-style
                              {:max-width "640px" :width "100%"})}]
         children)])

(defn button
  ([label on-click] (button label on-click {}))
  ([label on-click opts]
   [:button (merge (use-style styles/button-primary)
                   {:on-click on-click}
                   opts)
    label]))

(defn secondary-button [label on-click]
  [:button (merge (use-style styles/button-secondary)
                  {:on-click on-click})
   label])

(defn spinner []
  [:div {:style {:display         "flex"
                 :justify-content "center"
                 :padding         "32px"}}
   [:div {:style {:width           "48px"
                  :height          "48px"
                  :border          (str "4px solid " (:bg-surface styles/palette))
                  :border-top      (str "4px solid " (:accent styles/palette))
                  :border-radius   "50%"
                  :animation       "spin 1s linear infinite"}}]])

(defn card [& children]
  (into [:div (use-style styles/card-style)] children))

(defn heading [text]
  [:h1 {:style {:color       (:text-light styles/palette)
                :font-size   "2rem"
                :font-weight "700"
                :margin      "0 0 8px"}}
   text])

(defn subheading [text]
  [:p {:style {:color       (:text-muted styles/palette)
               :font-size   "1rem"
               :margin      "0 0 24px"}}
   text])
