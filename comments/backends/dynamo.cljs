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
  [client payload]
  (let [cmd-input {:TableName "BlogComment"
                   :Item payload}
        cmd (dynamo/PutItemCommand. (bean/->js cmd-input))]
    (.send client cmd)))


(defn build-dynamo-comment-payload
  [comment-input]
  {:PostId {:S (:post-id comment-input)}
   :CommentId {:S (uuid/v4)}
   :Message {:S (:message comment-input)}
   :Time {:S (.toISOString (js/Date.))}
   :Author {:S (:author comment-input)}})


(defn add-comment
  [new-comment]
  (let [client (dynamo/DynamoDBClient. {:region "us-east-1"})
        comment-payload (build-dynamo-comment-payload new-comment)]
    (p/do!
      (add-dynamo-comment client comment-payload))))

(comment
  (p/do! (add-comment {:post-id "clojure-bandits" :message "Where's the canoli?" :author "Tony Blundetto"}))
   
  (p/do!
    (p/let
     [r (add-comment {:post-id "clojure-bandits" :message "Where's the canoli?" :author "Tony Blundetto"})]
     (def test r)))
  (get-in (bean/->clj test) [:$metadata :httpStatusCode]))

(defn main []
  (p/let [r (create-comment-table)]
    r))

