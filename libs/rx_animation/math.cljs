(ns rx-animation.math)

(defn toRadians [deg]
  (* (/ (.-PI js/Math) 180) deg))

(defn deg->pos [deg]
  (let [radians (toRadians deg)]
    [(Math/cos radians) (Math/sin radians)]))

(defn distance [d] (fn [t] (* d t)))

(defn torque [deg] (fn [t] (* deg t)))
