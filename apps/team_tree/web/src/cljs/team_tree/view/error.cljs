(ns team-tree.view.error
  (:require [stylefy.core :as stylefy :refer [use-style use-sub-style]]
            [clojure.string :as string]))

(def style
  {:margin 0
   :border 0
   :padding "2em"
   :box-sizing "border-box"
   :width "100vw"
   :height "100vh"
   :background-size "cover"
   :background-image "url(/images/error-background.jpg)"
   ::stylefy/sub-styles { :title { :font-size "3em"}}})

(defn- print-stacktrace [err]
  (let [html-br (partial string/join "<br/>")]
    (-> err
        (.-stack)
        (string/split-lines)
        (html-br))))

(defn error-view [info]
  [:div (use-style style)
    [:h1 (use-sub-style style :title) "Sorry: Tracing the error."]
    [:h3 (.-message info)]
    [:aside {:dangerouslySetInnerHTML {:__html (print-stacktrace info)}}]])
