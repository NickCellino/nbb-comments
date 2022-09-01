(ns local-server
  (:require [deps]
            [repos.local]
            [htmx]
            [express]))

(def htmx-config
  (htmx/make-htmx-config
    {:repo :local
     :recaptcha-sitekey "6LcMAZQhAAAAAJKuFrianjr-xP8XIOysos4qeR4S"}))

(def express-config
  (express/make-express-config
    {:htmx-config htmx-config
     :recaptcha-secret js/process.env.RECAPTCHA_SECRET}))

(def express-app
  (express/create-app express-config))

(def server (express/start-server express-app))

(comment
  (express/stop-server server))

