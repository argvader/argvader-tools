(ns rx-animation.easing
  (:require-macros [rx-animation.easing :refer [defease]]))

(def ^:dynamic *easing-fns*
  (atom {}))

(defn add-easing-fn! [key fns]
  (swap! *easing-fns* assoc key fns))

(defn find-easing [keyword]
  (get @*easing-fns* keyword))

(defn list-easing-fns []
  (sort (keys @*easing-fns*)))

; based on github.com/mattdesl/eases
(defease back-in
  (fn [t]
    (let [s 1.70158]
      (* t t (- (* (+ s 1) t) s)))))

(defease back-out
  (fn [t]
    (let [t1- (- t 1)
          s 1.70158]
      (+ 1 (* t1- t1- (+ (* (+ s 1) t1-) s))))))

(defease back-in-out
  (fn [t]
    (let [s (* 1.70158 1.525)
          t2* (* t 2)
          t2- (- t2* 2)]
      (cond (< t2* 1.0)
        (* 0.5 (* t2* t2* (- (* (+ s 1) t2*) s)))
        :else (* 0.5 (+ 2 (* t2- t2- (+ s (* (+ s 1) t2-)))))))))

(defease bounce-out
  (fn [t]
    (let [a (/ 4.0 11.0)
          b (/ 8.0 11.0)
          c (/ 9.0 10.0)
          ca (/ 4356.0 361.0)
          cb (/ 35442.0 1805.0)
          cc (/ 16061.0 1805.0)
          t2 (* t t)]
     (cond (< t a) (* 7.5625 t2)
           (< t b) (+ 3.4 (- (* 9.075 t2) (* 9.9 t)))
           (< t c) (+ cc (- (* ca t2) (* cb t)))
           :else (+ 10.72 (- (* 10.8 t t) (* t 20.52)))))))

(defease bounce-in
  (fn [t]
   (let [bounce-out (find-easing :bounce-out)]
     (- 1.0 (bounce-out (- 1.0 t))))))

(defease bounce-in-out
  (fn [t]
    (let [bounce-out (find-easing :bounce-out)]
      (cond (< t 0.5) (* 0.5 (- 1.0 (bounce-out (- 1.0 (* 2.0 t)))))
            :else (+ 0.5 (* 0.5 (bounce-out (- (* 2.0 t) 1.0))))))))

(defease circ-in
  (fn [t]
    (- 1.0 (Math/sqrt (- 1.0 (* t t))))))

(defease circ-out
  (fn [t]
    (let [t-minus-1 (- t 1)]
      (Math/sqrt (- 1.0 (* t-minus-1 t-minus-1))))))

(defease circ-in-out
  (fn [t]
    (let [t2* (* t 2.0)
          t2- (- t2* 2.0)]
      (cond (< t2* 1.0)
        (* -0.5 (- (Math/sqrt (- 1 (* t2* t2*))) 1))
        :else (* 0.5 (+ 1 (Math/sqrt (- 1 (* t2- t2-)))))))))

(defease cubic-in
  (fn [t]
    (* t t t)))

(defease cubic-out
  (fn [t]
    (let [f (- t 1.0)]
      (+ (* f f f) 1.0))))

(defease cubic-in-out
  (fn [t]
   (cond (< t 0.5)
     (* 4.0 t t t)
     :else (+ 1.0 (* 0.5 (Math/pow (- (* 2.0 t) 2.0) 3.0))))))

(defease elastic-out
  (fn [t]
    (+ 1.0 (* (Math/sin (* -13.0 (+ t 1.0) (/ Math/PI 2))) (Math/pow 2.0 (* -10.0 t))))))

(defease elastic-in
  (fn [t]
    (* (Math/sin (* 13.0 t (/ Math/PI 2))) (Math/pow 2.0 (* 10.0 (- t 1.0))))))

(defease elastic-in-out
  (fn [t]
    (cond (< t 0.5) (* 0.5 (Math/sin (* 13.0 (/ Math/PI 2) 2.0 t)) (Math/pow 2.0 (* 10.0 (- (* 2.0 t) 1.0))))
          :else (+ 1.0 (* 0.5 (Math/sin (* -13.0 (/ Math/PI 2) (+ 1.0 (- (* 2.0 t) 1.0)))) (Math/pow 2.0 (* -10.0 (- (* 2.0 t) 1.0))))))))

(defease linear
  (fn [t]
    t))

(defease quad-in
  (fn [t]
    (* t t)))

(defease quad-out
  (fn [t]
    (let [t-inv (* -1 t)]
      (* t-inv (- t 2.0)))))

(defease quad-in-out
  (fn [t]
    (let [t05 (/ t 0.5)
          t1- (- t05 1.0)]
      (cond (< t05 1.0)
        (* t05 t05 0.5)
        :else
        (* -0.5 (- (* t1- (- t1- 2)) 1))))))

(defease quartic-in
  (fn [t]
    (Math/pow t 4.0)))

(defease quartic-out
  (fn [t]
    (+ 1.0 (* (Math/pow (- t 1.0) 3.0) (- 1.0 t)))))

(defease quartic-in-out
  (fn [t]
    (cond (< t 0.5)
      (* 8.0 (Math/pow t 4.0))
      :else (+ 1.0 (* -8.0 (Math/pow (- t 1.0) 4.0))))))

(defease sine-in
  (fn [t]
    (let [v (Math/cos (* t Math/PI 0.5))]
      (cond (< (Math/abs v) 1e-14)
        1
        :else (- 1 v)))))

(defease sine-out
  (fn [t]
   (Math/sin (* t (/ Math/PI 2)))))

(defease sine-in-out
  (fn [t]
    (* -0.5 (- (Math/cos (* Math/PI t)) 1))))
