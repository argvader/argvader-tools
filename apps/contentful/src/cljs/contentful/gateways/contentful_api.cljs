(ns contentful.gateways.contentful-api
    (:require-macros [cljs.core.async.macros :refer [go]]
                     [adzerk.env :as env])
    (:require [contentful.utils.axios :refer [axios-get axios-post axios-delete]]
              [cljs.core.async.interop :refer-macros [<p!]]
              [clojure.string :as str]))

(env/def CONTENTFUL_TOKEN :required)
(def org-id "3FD9k3JKrjuhQoE6WgTxRl")
(def api-url "https://api.contentful.com")
(def headers (clj->js {:Authorization (str "Bearer " CONTENTFUL_TOKEN) :Content-Type "application/vnd.contentful.management.v1+json"}))
(def using-names (map #(conj {} {:name (:name %) :id (get-in % [:sys :id])})))
(def using-membership (map #(conj {} {:id (get-in % [:sys :space :sys :id])})))
(def using-team-membership (map #(conj {} {:membership-id (get-in % [:sys :id]) :team-id (get-in % [:sys :team :sys :id])})))

(defn- map->form [m]
     (clojure.string/join "&" (for [[k v] m] (str (name k) "=" v))))

(defn- format-items [formatter response]
  (let [keyed-response (js->clj response :keywordize-keys true)
        items (get-in keyed-response [:data :items])]
    (transduce formatter conj items)))

(defn- format-includes [formatter key response]
  (let [keyed-response (js->clj response :keywordize-keys true)
        items (get-in keyed-response [:data :includes key])]
    (transduce formatter conj items)))

(defn- format-type [response]
  (let [keyed-response (js->clj response :keywordize-keys true)
        sys (get-in keyed-response [:data :sys])]
    (conj {} {:id (:id sys) :type (:type sys)})))


(defn find-sys-id [email]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/users?limit=1&query=" (str/replace email #"@" "%40"))
          response    (<p! (axios-get request-url headers))]
      (-> (format-items using-names response)
          (first)
          (:id)))))

(defn find-user-org-id [email]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/organization_memberships?limit=1&query=" (str/replace email #"@" "%40"))
          response    (<p! (axios-get request-url headers))]
      (-> (format-items using-names response)
          (first)
          (:id)))))

(defn list-spaces []
  (go
    (let [request-url (str api-url "/organizations/" org-id "/spaces?limit=100")
          response (<p! (axios-get request-url headers))]
      (format-items using-names response))))

(defn user-space-membership [user-id space-id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/space_memberships?include=roles,sys.space,sys.createdBy&limit=100&sys.space.sys.id=" space-id "&sys.user.sys.id=" user-id)
          response (<p! (axios-get request-url headers))]
      (->> (format-items using-names response)
          (map #(:id %))
          (distinct)
          (first)))))

(defn space-memberships [id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/space_memberships?include=roles,sys.space,sys.createdBy&limit=100&sys.user.sys.id=" id)
          response (<p! (axios-get request-url headers))]
      (->> (format-includes using-names :Space response)
          (map #(:id %))
          (distinct)))))

(defn add-space [space-id user-email]
  (go
    (let [request-url (str api-url "/spaces/" space-id "/space_memberships")
          response (<p! (axios-post request-url headers {:admin true :email user-email}))]
      (format-type response))))

(defn remove-space [space-id membership-id]
  (go
    (let [request-url (str api-url "/spaces/" space-id "/space_memberships/" membership-id)
          response (<p! (axios-delete request-url headers))]
      (format-type response))))

(defn list-teams []
  (go
    (let [request-url (str api-url "/organizations/" org-id "/teams?limit=100")
          response (<p! (axios-get request-url headers))]
      (format-items using-names response))))

(defn user-team-membership [user-org-id team-id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/team_memberships?limit=100&include=sys.team&sys.organizationMembership.sys.id=" user-org-id)
          response (<p! (axios-get request-url headers))]
      (->>
         (format-items using-team-membership response)
         (filter #(= team-id (:team-id %)))
         (first)))))

(defn team-memberships [id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/team_memberships?limit=100&include=sys.team&sys.organizationMembership.sys.id=" id)
          response (<p! (axios-get request-url headers))]
      (format-includes using-names :Team response))))

(defn add-team [team-id org-membership-id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/teams/" team-id "/team_memberships")
          response (<p! (axios-post request-url headers {:organizationMembershipId org-membership-id}))]
      (format-type response))))

(defn remove-team [team-id membership-id]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/teams/" team-id "/team_memberships/" membership-id)
          response (<p! (axios-delete request-url headers))]
      (format-type response))))

(defn registered? [email]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/users?query=" (str/replace email #"@" "%40"))
          response (<p! (axios-get request-url headers))]
      (->> response
           (format-items using-names)
           (empty?)
           (not)))))

(defn invite [email]
  (go
    (let [request-url (str api-url "/organizations/" org-id "/invitations")
          response (<p! (axios-post request-url headers {:email email :role "developer"}))]
       (format-type response))))
