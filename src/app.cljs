(ns app
  (:require [htmx]
            [express]))

(defn init-app
  [htmx-config express-config]
  (let [htmx-instance (htmx/configure-htmx htmx-config)
        express-instance (express/configure-express (express-config htmx-instance))]
    {:htmx htmx-instance
     :express express-instance}))

