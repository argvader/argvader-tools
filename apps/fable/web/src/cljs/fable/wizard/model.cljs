(ns fable.wizard.model)

(def steps [:connect :media :theme :generate :preview :export])

(defn next-step [current]
  (let [idx (.indexOf (clj->js steps) current)]
    (when (< idx (dec (count steps)))
      (nth steps (inc idx)))))

(defn prev-step [current]
  (let [idx (.indexOf (clj->js steps) current)]
    (when (> idx 0)
      (nth steps (dec idx)))))
