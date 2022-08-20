(ns dynamo
  (:require ["@aws-sdk/client-dynamodb" :as dynamo]
            [promesa.core :as p]))

"
Partition key should be post-id, string
Sort key should be some kind of unique comment id
"

(defn create-comment-table
  [] (let [cmd-input #js {:TableName "BlogComment"
                          :AttributeDefinitions #js [#js {:AttributeName "PostId"
                                                          :AttributeType "S"}
                                                     #js {:AttributeName "CommentId"
                                                          :AttributeType "S"}]
                          :KeySchema #js [#js {:KeyType "HASH"
                                               :AttributeName "PostId"}
                                          #js {:KeyType "RANGE"
                                               :AttributeName "CommentId"}]
                          :ProvisionedThroughput #js {:ReadCapacityUnits 1
                                                      :WriteCapacityUnits 1}}
           cmd (dynamo/CreateTableCommand. cmd-input)
           client (dynamo/DynamoDBClient. {:region "us-east-1"})]
        (.send client cmd)))

(defn main [] 4)

(def foo #js [1 2 3])

(comment (def result
            (p/let [r (create-comment-table)]
              r)))

(comment (.then
          (create-comment-table)
          (fn [success] (println "success" success))
          (fn [error] (println "error" error))))


