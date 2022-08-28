(ns run-local
  (:require [app]
            [backends.local :as backend]))

(def htmx-config {:comment-form-id "comment-form"
                  :comment-list-div-id "comments-list"
                  :post-comment-url "http://localhost:3000/comments"
                  :save-comment-fn backend/add-comment
                  :get-comments-fn backend/list-comments})

(defn express-config
  [htmx]
  {:post-comment (:post-comment htmx)
   :get-comments (:get-comments htmx) 
   :gen-comments-form-html (:gen-comments-form-html htmx)
   :allowed-origin-url "http://localhost:8080"
   :port "3000"})

(def system (app/run-app htmx-config express-config))

(comment
  (app/stop-app system))

