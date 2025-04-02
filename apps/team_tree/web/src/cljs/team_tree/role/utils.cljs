(ns team-tree.role.utils)

(def roles ["product lead" "UX lead" "tech lead" "engineer" "data science"])


(defn filter-role [role-name members]
  (let [list (filter #(= (:role %) role-name) members)]
     (if (empty? list)
        [{:role role-name :person nil}]
        (vec list))))

(defn role-sort [members]
  (let [result (flatten (vec (map #(filter-role % members) roles)))]
     result))
