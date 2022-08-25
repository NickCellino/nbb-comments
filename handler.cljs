(ns handler
   (:require
     [deps]
     [comments.express :as exp]
     [comments.backends.local :as backend]
     ["serverless-http$default" :as serverless]
     [nbb.classpath]))

(def app
  (exp/create-app
    backend/add-comment
    backend/list-comments))

(def serverless-app (serverless app))

#js {:handler serverless-app}
