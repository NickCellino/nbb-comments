(ns lambda
  (:require [app]
            [backends.dynamo :as backend]
            [express]
            ["serverless-http$default" :as serverless]))

(def lambda-base-url js/process.env.LAMBDA_BASE_URL)
(def allowed-origin-url js/process.env.ALLOWED_ORIGIN_URL)

(def htmx-config {:comment-form-id "comment-form"
                  :comment-list-div-id "comments-list"
                  ; TODO use env var here
                  :post-comment-url "https://q79hj072qf.execute-api.us-east-1.amazonaws.com/comments"
                  :save-comment-fn backend/add-comment
                  :get-comments-fn backend/list-comments})

(defn express-config
  [htmx]
  {:post-comment (:post-comment htmx)
   :get-comments (:get-comments htmx) 
   :gen-comments-form-html (:gen-comments-form-html htmx)
   ; TODO use env var here
   :allowed-origin-url "http://localhost:8080"})

(def system (app/init-app htmx-config express-config))

(def app (get-in system [:express :app]))

(def serverless-app (serverless app))

#js {:handler serverless-app}
