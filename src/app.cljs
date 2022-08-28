(ns app
  (:require [htmx]
            [express]))

(defn run-app
  [htmx-config express-config]
  (let [htmx-instance (htmx/configure-htmx htmx-config)
        express-instance (express/configure-express (express-config htmx-instance))]
    {:htmx htmx-instance
     :express express-instance}))

(defn stop-app
  [system]
  (let [server (get-in system [:express :server])]
    (express/stop-server server)
    (.log js/console "Server has stopped listening")))

