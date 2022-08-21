(ns handler)

(defn handler [event _ctx]
      (js/console.log event)
      (js/Promise.resolve
        (clj->js {:statusCode 200
                  :body       (js/JSON.stringify #js{:message "Hello from Clojurescript!"})})))

#js {:handler handler}
