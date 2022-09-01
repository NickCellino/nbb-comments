(ns lambda
  (:require [app]
            [repos.dynamo]
            [express]
            [htmx]
            ["serverless-http$default" :as serverless]))

; parameters from the environment
(def lambda-base-url js/process.env.LAMBDA_BASE_URL)
(def allowed-origin-url js/process.env.ALLOWED_ORIGIN_URL)
(def recaptcha-secret js/process.env.RECAPTCHA_SECRET)
(def recaptcha-sitekey js/process.env.RECAPTCHA_SITEKEY)

(def htmx-config
  (htmx/make-htmx-config
    {:repo :dynamo
     :recaptcha-sitekey recaptcha-sitekey
     :post-comment-url (str lambda-base-url "/comments")}))

(def express-config
  (express/make-express-config
    {:htmx-config htmx-config
     :recaptcha-secret recaptcha-secret
     :allowed-origin-url allowed-origin-url}))

(def express-app
  (express/create-app express-config))

(def serverless-app (serverless express-app))

#js {:handler serverless-app}

