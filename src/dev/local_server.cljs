(ns dev.local-server
  (:require [deps]
            [dev.local-repo]
            [dynamo-repo]
            [htmx]
            [express]))

(def htmx-config
  (htmx/make-htmx-config
    {:repo :local}))

(def express-config
  (express/make-express-config
    {:htmx-config htmx-config
     :static-files-root "src/dev"
     :recaptcha-enabled false}))

(def express-app
  (express/create-app express-config))

(def server (express/start-server express-app))

(comment
  (express/stop-server server))

