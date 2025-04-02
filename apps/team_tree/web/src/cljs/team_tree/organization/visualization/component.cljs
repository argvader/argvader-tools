(ns team-tree.organization.visualization.component
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [router.core :as router]
            ["d3" :as d3]
            [stylefy.core :refer [use-style sub-style]]
            [team-tree.organization.visualization.styles :as styles]))

(def margin {
             :top 50
             :bottom 90
             :left 20
             :right 20})

(defn- define-container [node width height]
  (-> node
      (.append "svg")
      (.attr "width" width)
      (.attr "height" height)
      (.append "g")
      (.attr "transform" (str "translate(" (:left margin) "," 0 ")"))))

(defn- transform-data [data]
   (let [transformed (reduce-kv (fn [m k v] (if (= k :teams) (assoc m :children v) (assoc m k v))) {} data)]
     (.sum (.hierarchy d3 (clj->js transformed)) #(.-size %))))

(defn- display-name [data]
  (str (.. data -data -name) " (" (- (.-value data) 1) ")"))

(defn- render-text [node data]
  (-> node
      (.selectAll "text")
      (.data (.leaves data))
      (.enter)
      (.append "text")
      (.attr "x" #(+ 14 (.-x0 %)))
      (.attr "y" #(+ 21 (.-y0 %)))
      (.text #(display-name %))
      (.attr "font-size" "15px")
      (.attr "fill" "white")))


(defn- build-graph [node data]
  (let [
        width  (- (.-innerWidth js/window) (:left margin) (:right margin))
        height (- (.-innerHeight js/window) (:top margin) (:bottom margin))
        root (define-container node width height)
        square (.. d3 -treemapSquarify -ratio)
        scrubbed-data (transform-data data)
        graph (-> d3
                  (.treemap)
                  (.tile  (square 1))
                  (.size (array width height))
                  (.padding 2))]

    (graph scrubbed-data)
    (-> root
        (.selectAll "rect")
        (.data (.leaves scrubbed-data))
        (.enter)
        (.append "rect")
        (.attr "cursor" "pointer")
        (.on "click" #(set! (.. js/window -location -href) (router/url-for :domain :id (.. %2 -data -id))))
        (.attr "x" #(.-x0 %))
        (.attr "y" #(.-y0 %))
        (.attr "width" #(- (.-x1 %) (.-x0 %)))
        (.attr "height" #(- (.-y1 %) (.-y0 %)))
        (.attr "class" (get (use-style styles/graph) :class)))
   (render-text root scrubbed-data)))

(defn- render-graph [node data]
  (if (seq data)
     (build-graph node (assoc {} :teams data))))

(defn component [data]
  (let [!ref (atom nil)]
    (r/create-class
      {:display-name "domains"
       :component-did-mount #(render-graph (. d3 select @!ref) data)
       :component-did-update (fn [this] (let [[_ data] (r/argv this)]
                                          (render-graph (. d3 select @!ref) data)))
       :reagent-render
         (fn []
            [:div {:ref (fn [node] (reset! !ref node))}])})))
