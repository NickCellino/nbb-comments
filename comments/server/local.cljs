(ns comments.server.local
  (:require
    [comments.express :as exp]
    [comments.backends.local :as backend]))

(def app
  (exp/create-app
    backend/add-comment
    backend/list-comments
    "http://localhost:3000/comments"
    "http://localhost:8080"))


(def server
  (exp/start-server app 3000 (fn []
                               (.log js/console "Listening on port 3000..."))))

(comment
  (exp/stop-server server))
