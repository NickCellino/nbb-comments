(ns comments.server.local
  (:require [comments.express :as exp]))

(def app (exp/create-app))

(.listen app 3000 (fn []
                    (.log js/console "Listening on port 3000...")))
