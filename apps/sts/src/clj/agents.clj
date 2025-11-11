(ns agents)

;; make 10 agents initialized to zero
(def sums (map agent (repeat 10 0)))

(def numbers (range 1000000)) ;; one million numbers

;; loop through all numbers and round-robin the agents
(doseq [[x agent] (map vector numbers (cycle sums))]
  (send agent + x))

;; wait at most 10 seconds
(apply await-for 10000 sums)

;; sum up the answers in all ten agents
(println (apply + (map deref sums)))
