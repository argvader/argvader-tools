(ns team-tree.role.model)

(defprotocol RoleInterface
  (role-name [this])
  (role-identity [this]))

(deftype TeamRole [name person]
  RoleInterface
  (role-name [this] name)
  (role-identity [this]
    person))
