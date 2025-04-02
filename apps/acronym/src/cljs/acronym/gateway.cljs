(ns acronym.gateway
   (:require ["@aws-sdk/client-dynamodb" :as DB]
             ["@aws-sdk/lib-dynamodb" :as dbLib]
             [cljs.core.async :refer [go]]
             [cljs.core.async.interop :refer-macros [<p! <!]]))

(def client (new DB/DynamoDBClient (js-obj "region" "us-east-1")))
(def docClient (.from dbLib/DynamoDBDocumentClient client))
(def table_name "asurion_acronyms")

(defn- build-definition-list [item new-definition]
  (if (nil? item)
    #js[new-definition]
    (let [current (.-definition item)]
       (.add current new-definition)
       (.from js/Array current))))

(defn get-define [acronym]
  (go
    (let [params (js-obj "TableName" table_name
                         "Key" (js-obj
                                "acronym" acronym))
          data (<p! (.send docClient (new dbLib/GetCommand params)))]
      (aget data "Item"))))

(defn put-define! [acronym definition]
  (go
    (let [
          item (<! (get-define acronym))
          params (js-obj "TableName" table_name
                          "Item" (js-obj
                                   "acronym" #js{ "S" acronym}
                                   "definition" (js-obj "SS" (build-definition-list item definition))))]
         (<p! (.send client (new DB/PutItemCommand params))))))

(defn list-definitions [])
