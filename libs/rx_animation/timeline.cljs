(ns rx-animation.timeline
  [:require [beicon.core :as core]
            [rx-animation.easing :refer [find-easing list-easing-fns]]])

(def rx js/rxjsMain)
(def rxop js/rxjsOperators)
(def initial-value (core/from [0]))

(def all-easing-fns (list-easing-fns))

(defn mixin
  ([ob] ob)
  ([f ob] (core/map f ob)))

(defn- scheduled-interval
  "Returns an observable sequence that produces a
  value after each period."
  [ms scheduler]
  (.interval rx ms scheduler))

(defn- repeat-anim
  ([ob]
   (core/pipe ob (.repeat ^js rxop)))
  ([count ob]
   (core/pipe ob (.repeat ^js rxop count))))

(defn- defer
  "Provides wrapper for the rxjs defer operation"
  [fn]
  {:pre [(fn? fn)]}
  (.defer rx fn))

(defn- take-while-inclusive
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f inclusive ob]
  (core/pipe ob (.takeWhile ^js rxop #(boolean (f %)) inclusive)))

(defn- partial-time [scheduler]
  (let [start (.now scheduler)]
     (->>
       (scheduled-interval 0 scheduler)
       (core/map #(- (.now scheduler) start)))))

(defn ms-elapsed
  ([] (ms-elapsed (core/scheduler :animation-frame)))
  ([scheduler]
   (defer (fn [] (partial-time scheduler)))))

(defn elapsed-animation
  ([handler] (elapsed-animation handler nil))
  ([handler modifiers]
   (let [ob (ms-elapsed)]
     (core/subscribe ob handler))))

(defn duration
  ([ms] (duration ms (core/scheduler :animation-frame)))
  ([ms scheduler]
   (->> scheduler
       (ms-elapsed)
       (core/map (fn [ems] (/ ems ms)))
       (take-while-inclusive (fn [t] (<= t 1)) true)
       (core/map (fn [value] (max (min value 1.0) 0.0)))
       (core/concat initial-value))))

(defn with-ease [easing timeline]
  (let [ease-fn (find-easing easing)]
    (core/map ease-fn timeline)))

(defn looping
  ([timeline]
   (repeat-anim timeline))
  ([count timeline]
   (repeat-anim count timeline)))

(defn bouncing
  ([timeline]
   (bouncing nil timeline))
  ([count timeline]
   (let [reverse (->> timeline
                     (core/map (fn [x] (- 1 x))))
         sources (core/concat-all (core/of timeline reverse))]
      (if (nil? count)
        (looping sources)
        (looping count sources)))))
