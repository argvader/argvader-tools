(ns contentful.utils.axios
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require ["axios" :as axios]
              ["https" :as https]))

(def https-agent (https/Agent. (clj->js {:rejectUnauthorized false})))

(defn axios-get
  ([request-url headers]
   (axios/get request-url (clj->js {:httpsAgent https-agent :headers headers})))
  ([request-url]
   (axios/get request-url (clj->js {:httpsAgent https-agent}))))

(defn axios-post [request-url headers data]
  (let [js-data (clj->js data)
        js-config (clj->js {:httpsAgent https-agent :headers headers})]
    (axios/post request-url js-data js-config)))

(defn axios-delete [request-url headers]
  (axios/delete request-url (clj->js {:httpsAgent https-agent :headers headers})))
