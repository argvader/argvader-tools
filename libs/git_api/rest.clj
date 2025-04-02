(ns git-api.rest
  (:require [hato.client :as hc]
            [cheshire.core :refer [parse-string]]))

(def page-size 100)
(def authorizer (atom ""))
(def client (hc/build-http-client {:connect-timeout 10000
                                   :redirect-policy :always}))

(defn init [token]
  (reset! authorizer token))

(defn close []
  (reset! authorizer ""))

(defn- echo [data]
  (println data)
  data)

(defn- load-contributors [organization repo page results]
  (let [url (format "https://api.github.com/repos/%s/%s/stats/contributors?per_page=%n&page=%s", organization, (:name repo), page-size, page)
        contributors (-> url
                         (hc/get {:http-client client
                                  :headers {
                                            "Authorization" (str "bearer " @authorizer)
                                            "Accept" "application/vnd.github.v3+json"}})
                         (:body)
                         (parse-string true))]
      (if (= (count contributors) page-size)
        (load-contributors organization repo (+1 page) (concat results contributors))
        (concat results contributors))))

(defn find-contributors [organization repo]
  (load-contributors organization repo 0 []))
