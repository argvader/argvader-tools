(ns team-tree.person.resolver
  (:require
       [team-tree.person.data :as data]
       [team-tree.person.metrics :as metrics]))

(defn resolver-map
  []
  {:query/person-by-id data/resolve-person-by-id
   :resolver/team-member data/resolve-person-by-team-member
   :resolver/product-lead data/resolve-person-by-product-lead
   :resolver/design-lead data/resolve-person-by-design-lead
   :resolver/engineering-lead data/resolve-person-by-engineering-lead
   :resolver/person-code-metrics metrics/resolve-code-metrics})
