(ns dynamo
  (:require ["@aws-sdk/client-dynamodb" :as dynamo]
            ["uuid" :as uuid]
            [promesa.core :as p]
            [cljs-bean.core :as bean]))

"
Partition key should be post-id, string
Sort key should be some kind of unique comment id
"

(defn create-comment-table
  [] (let [cmd-input {:TableName "BlogComment"
                      :AttributeDefinitions [{:AttributeName "PostId"
                                              :AttributeType "S"}
                                             {:AttributeName "CommentId"
                                              :AttributeType "S"}]
                      :KeySchema [{:KeyType "HASH"
                                   :AttributeName "PostId"}
                                  {:KeyType "RANGE"
                                   :AttributeName "CommentId"}]
                      :ProvisionedThroughput {:ReadCapacityUnits 1
                                              :WriteCapacityUnits 1}}
           cmd (dynamo/CreateTableCommand. (bean/->js cmd-input))
           client (dynamo/DynamoDBClient. {:region "us-east-1"})]
        (.send client cmd)))

(defn add-dynamo-comment
  [client post-id comment-id message]
  (let [cmd-input {:TableName "BlogComment"
                   :Item {:PostId {:S post-id}
                          :CommentId {:S comment-id}
                          :Message {:S message}}}
        cmd (dynamo/PutItemCommand. (bean/->js cmd-input))]
    (.send client cmd)))


(defn add-comment
  [message]
  (let [comment-id (uuid/v4)
        client (dynamo/DynamoDBClient. {:region "us-east-1"})]
    (p/do!
      (add-dynamo-comment client "clojure-bandits" comment-id message))))

(def result (add-comment "hello world"))

(comment
  (p/let
    [r (add-comment "is this thing on?")]
    (def test r))
  (get-in (bean/->clj test) [:$metadata :httpStatusCode]))

(defn main []
  (p/let [r (create-comment-table)]
    r))

