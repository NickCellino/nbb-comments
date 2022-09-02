(ns recaptcha
  (:require ["axios$default" :as axios]
            [promesa.core :as p]))

(defn verify
  [token threshold secret]
  (p/let
    [response (axios/post
               "https://www.google.com/recaptcha/api/siteverify"
               #js {:secret secret
                    :response token}
               #js {:headers #js {"Content-Type" "multipart/form-data"}})
     data (aget response "data")
     success (aget data "success")]
    (if (not success)
      (do
        (.log js/console "Recaptcha verify failed:" data)
        {:verified false})
      (let [score (aget data "score")]
        (.log js/console "Recaptcha score:" score)
        {:verified (> score threshold) :score score}))))

