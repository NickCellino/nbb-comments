(ns dynamo-repo
  (:require ["@aws-sdk/client-dynamodb" :as dynamo]
            ["uuid" :as uuid]
            [promesa.core :as p]
            [cljs-bean.core :as bean]
            [repo]))

(defn add-dynamo-comment
  [client comment-table-name payload]
  (let [cmd-input {:TableName comment-table-name
                   :Item payload}
        cmd (dynamo/PutItemCommand. (bean/->js cmd-input))]
    (.send client cmd)))

(defn build-dynamo-comment-payload
  [comment-input]
  {:PostId {:S (:post-id comment-input)}
   :CommentId {:S (uuid/v4)}
   :Message {:S (:message comment-input)}
   :Time {:S (.toISOString (js/Date.))}
   :Author {:S (:author comment-input)}
   :RecaptchaScore {:S (str (:score comment-input))}})

(defn add-comment
  [{:keys [comment-table-name] } new-comment]
  (let [client (dynamo/DynamoDBClient. {:region "us-east-1"})
        comment-payload (build-dynamo-comment-payload new-comment)]
    (p/do!
      (add-dynamo-comment client comment-table-name comment-payload)
      new-comment)))

(defmethod repo/save-comment :dynamo
  [config new-comment]
  (add-comment config new-comment))

(defn list-dynamo-comments
  [client comment-table-name post-id]
  (let [cmd-input {:TableName comment-table-name
                   :KeyConditionExpression "PostId = :postid"
                   :ExpressionAttributeValues {":postid" {:S post-id}}}
        cmd (dynamo/QueryCommand. (bean/->js cmd-input))]
    (.send client cmd)))

(def field-mapping
  {:PostId :post-id
   :Message :message
   :CommentId :comment-id
   :Time :time
   :Author :author})

(defn de-dynamoify-comment
  [dynamo-comment]
  (let [name-mapping-fn (fn [[k v]] [(k field-mapping) v])
        value-extract-fn (fn [[k v]] [k (:S v)])]
    (into {} (map (comp name-mapping-fn value-extract-fn) dynamo-comment))))

(defn list-comments
  [{:keys [comment-table-name]} post-id]
  (p/let [client (dynamo/DynamoDBClient. {:region "us-east-1"})
          js-comments (list-dynamo-comments client comment-table-name post-id)
          clj-comments (:Items (bean/->clj js-comments))]
    (reverse (sort-by :time (map de-dynamoify-comment clj-comments)))))

(defmethod repo/get-comments :dynamo
  [config post-id]
  (list-comments config post-id))

