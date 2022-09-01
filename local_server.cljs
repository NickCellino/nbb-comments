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

(comment
  (require '[promesa.core :as p])
  (htmx/get-recaptcha-callback-js htmx-config)
  (p/let
    [cmts (htmx/get-comments htmx-config "clojure-bandits")]
    (def foo cmts))

  (defn express-config
   [htmx]
   {:post-comment (:post-comment htmx)
    :get-comments (:get-comments htmx) 
    :gen-comments-form-html (:gen-comments-form-html htmx)
    :allowed-origin-url "http://localhost:8080"
    :recaptcha-threshold 0.5
    :recaptcha-secret js/process.env.RECAPTCHA_SECRET})

  (def system (app/init-app htmx-config express-config))

  (def local-server (express/start-server
                     (get-in system [:express :app])
                     "3000"
                     (fn [] (.log js/console "Listening on port 3000")))))

(comment
  (get-in system [:htmx :submit-comment-button]))


(comment
  "Possible interface for running this."

  (def local-backend {})
  (htmx-api {:recaptcha-sitekey "6LcMAZQhAAAAAJKuFrianjr-xP8XIOysos4qeR4S"
             :comment-repo local-backend
             :post-comment-url "http://localhost:3000/comments"})

  (express-api {:backend htmx-api
                :allowed-origin-url "http://localhost:8080"
                :port "3000"
                :recaptcha-threshold 0.5
                :recaptcha-secret js/process.env.RECAPTCHA_SECRET})

  (run express-api))
      
