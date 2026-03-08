(ns fable.storybook.builder
  (:require
   [fable.ai.vision :as vision]
   [fable.ai.story :as story]
   [fable.storybook.model :as model]))

(defn build-storybook
  "Full pipeline: describe images with vision, then generate storybook text.
  Returns storybook map or throws on validation failure."
  [posts theme job-progress-fn]
  (let [total         (count posts)
        scene-descs   (mapv (fn [post idx]
                              (let [desc (vision/describe-image (:image-url post) theme)]
                                (job-progress-fn (* 70 (/ (inc idx) total)))
                                desc))
                            posts
                            (range))
        _             (job-progress-fn 75)
        storybook     (story/generate-storybook posts scene-descs theme)
        _             (job-progress-fn 95)]
    (when-not (model/valid? storybook)
      (throw (ex-info "Invalid storybook structure" {:explain (model/explain storybook)})))
    storybook))
