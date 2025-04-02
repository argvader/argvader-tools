(ns rx-animation.easing)

(defmacro ^{:arglists '([name & body])}
  defease
  "Add easing function to list"
  [name & body]
  `(let [key# ~(keyword name)
         action# ~@body]
     (rx-animation.easing/add-easing-fn! key# action#)))
