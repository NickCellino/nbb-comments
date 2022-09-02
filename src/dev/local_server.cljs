(ns dev.local-server
  (:require [deps]
            [dev.local-repo]
            [htmx]
            [express]))

(def htmx-config
  (htmx/make-htmx-config
    {:repo :local
     :recaptcha-sitekey js/process.env.RECAPTCHA_SITEKEY}))

(def express-config
  (express/make-express-config
    {:htmx-config htmx-config
     :recaptcha-secret js/process.env.RECAPTCHA_SECRET
     :static-files-root "src/dev"}))

(def express-app
  (express/create-app express-config))

(def server (express/start-server express-app))

(comment
  (express/stop-server server))

