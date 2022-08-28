(ns server.lambda
  (:require
    [comments.express :as exp]
    [comments.backends.dynamo :as backend]
    ["serverless-http$default" :as serverless]))

(def app
  (exp/create-app
    backend/add-comment
    backend/list-comments
    "https://q79hj072qf.execute-api.us-east-1.amazonaws.com/comments"
    "http://localhost:8080"))

(def serverless-app (serverless app))

#js {:handler serverless-app}
