(ns team-tree.organization.styles
    (:require [themes.typography :as typography]
              [themes.palette :refer [color ->hex shade]]))

(def organization
  {:font-family typography/basic
   :grid-area "content"
   :display "grid"
   :padding "0.5em"})
