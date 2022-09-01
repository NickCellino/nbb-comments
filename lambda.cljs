(ns lambda
  (:require [app]
            [backends.dynamo :as backend]
            [express]
            ["serverless-http$default" :as serverless]))

; parameters from the environment
(def lambda-base-url js/process.env.LAMBDA_BASE_URL)
(def allowed-origin-url js/process.env.ALLOWED_ORIGIN_URL)
(def recaptcha-secret js/process.env.RECAPTCHA_SECRET)

(def htmx-config {:recaptcha-sitekey "6LcMAZQhAAAAAJKuFrianjr-xP8XIOysos4qeR4S"
                  :comment-form-id "comment-form"
                  :comment-list-div-id "comments-list"
                  :post-comment-url (str lambda-base-url "/comments")
                  :save-comment-fn backend/add-comment
                  :get-comments-fn backend/list-comments})

(defn express-config
  [htmx]
  {:post-comment (:post-comment htmx)
   :get-comments (:get-comments htmx) 
   :gen-comments-form-html (:gen-comments-form-html htmx)
   :allowed-origin-url allowed-origin-url
   :recaptcha-threshold 0.5
   :recaptcha-secret recaptcha-secret})

(def express-app
  (let [system (app/init-app htmx-config express-config)]
    (get-in system [:express :app])))

(def serverless-app (serverless express-app))

#js {:handler serverless-app}
