(ns fable.storybook.model
  (:require [clojure.spec.alpha :as s]))

(s/def ::title string?)
(s/def ::tagline string?)
(s/def ::page-num pos-int?)
(s/def ::chapter string?)
(s/def ::body string?)
(s/def ::image-url string?)
(s/def ::scene-desc string?)

(s/def ::page
  (s/keys :req-un [::page-num ::chapter ::body]
          :opt-un [::image-url ::scene-desc]))

(s/def ::pages (s/coll-of ::page :min-count 1))

(s/def ::storybook
  (s/keys :req-un [::title ::tagline ::pages]))

(defn valid? [storybook]
  (s/valid? ::storybook storybook))

(defn explain [storybook]
  (s/explain-str ::storybook storybook))
