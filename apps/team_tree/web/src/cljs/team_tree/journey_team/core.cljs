(ns team-tree.journey-team.core
  (:require   [team-tree.role.component :as role]
              [team-tree.journey-team.model :as model]))

(defn toRadians [deg]
  (* (/ (.-PI js/Math) 180) deg))

(defn deg->pos [deg]
  (let [radians (toRadians deg)]
    [(Math/cos radians) (Math/sin radians)]))

(defn team-radius [count]
  (max (* (inc count) 24) 90))

(defn calc-position [radius degree offset]
  (map #(+ radius (* % radius)) (deg->pos (+ offset degree))))

(defn position-team [team radius offset]
   (let [length (count team)
         delta (quot 360 length)
         deltas (range 0 (* length delta) delta)
         position (partial calc-position radius)]
      (map (fn [member delta]
              (let [pos (position delta offset)]
                ^{:key (:id member)}[role/component member pos])) team deltas)))

(defn ->Journey-Team [data]
  (let [model (model/JourneyTeam. data)
        radius (team-radius (-> model (model/team-members) (count)))
        team (position-team (model/team-members model) radius 0)]
    {:name (:name data)
     :team team
     :radius radius}))
