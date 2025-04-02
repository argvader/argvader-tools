(ns promise.core
  #?(:cljs (:require-macros [promise.core :as promise])))

#?(:clj
    (defmacro promise->
      [promise & body]
      `(.catch
        (-> ~promise
            ~@(map (fn [expr] (list '.then expr)) body))
        (fn [error#]
          (prn "Promise rejected " {:error error#})))))
