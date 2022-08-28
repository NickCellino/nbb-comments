(ns run-local
  (:require [app]
            [configs.local :as config]))

(def system (app/run-app config/htmx-config config/express-config))

(comment
  (app/stop-app system))

