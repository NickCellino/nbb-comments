(ns comments.express
  (:require ["express$default" :as express]))

(defn create-app
  []
  (let [app (express)]
    (.get app "/" (fn [req res]
                    (.send res "<h1>Hello world!</h1>")))
    app))

