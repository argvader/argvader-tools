(ns team-tree.domain.utils)

(defn team-size [team]
  (count (:members team)))

(defn domain-size [domain]
  (if domain
    (->> domain
         (:teams)
         (map team-size)
         (reduce +)
         (+ 1))
    0))
