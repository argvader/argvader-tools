(ns team-tree.constants
  (:require [re-frame.interop :as interop :refer [ratom]]))

(def css-vendors {:vendors ["webkit"]
                  :auto-prefix #{"mask"}})

(defonce window-size (let [size (ratom {:width  (.-innerWidth  js/window)
                                        :height (.-innerHeight js/window)})]
                       (.addEventListener js/window "resize"
                         (fn [] (do
                                  (reset! size {:width  (.-innerWidth  js/window)
                                                :height (.-innerHeight js/window)}))))
                      size))
