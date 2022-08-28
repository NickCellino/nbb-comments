(ns local-server
  (:require [deps]
            [app]
            [backends.local :as backend]
            [express]))

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
   :allowed-origin-url "http://localhost:8080"})

(def system (app/init-app htmx-config express-config))

(def local-server (express/start-server
                   (get-in system [:express :app])
                   "3000"
                   (fn [] (.log js/console "Listening on port 3000"))))

(comment
  (express/stop-server local-server))

