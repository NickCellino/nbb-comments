(ns express
  (:require ["express$default" :as express]
            ["http" :as http]
            [recaptcha]
            [promesa.core :as p]
            [goog.object :as gobj]
            [htmx]))

(defn extract-param
  [param req]
  (let [param-type (:type param)
        val (cond
              (= param-type :body) (gobj/getValueByKeys req "body" (:name param))
              (= param-type :query) (gobj/getValueByKeys req "query" (:name param))
              :else nil)]
    (when (and (nil? val) (:required param))
      (throw (js/Error. (str "missing required parameter " param))))
    val))

(defn extract-params
  "Extract parameters from an expressjs request.

  Can extract params from body or query params.

  Can specify which fields are required.

  Example-usage:
    (extract-params
      [{:type :body :name \"message\" :required true}
       {:type :query :name \"post-id\"}]
      req)"
  [params req]
  (let [values (map #(extract-param % req) params)
        param-kw-names (map (comp keyword :name) params)]
    (into {} (map vector param-kw-names values))))

(defn get-comments-handler
  [{:keys [htmx-config]}]
  (fn [req res]
    (p/let [{:keys [post-id]} (extract-params [{:type :query :name "post-id" :required true}] req)
            list-comments-response (htmx/get-comments htmx-config post-id)]
      (.send res list-comments-response))))

(defn post-comment-handler
  [{:keys [htmx-config recaptcha-secret recaptcha-threshold]}]
  (fn [req res] 
    (p/let [payload-config [{:type :body :name "author" :required true}
                            {:type :body :name "message" :required true}
                            {:type :body :name "post-id" :required true}
                            {:type :body :name "g-recaptcha-response" :required true}]
            payload (extract-params payload-config req)
            add-comment-response (htmx/post-comment htmx-config payload)
            verified (recaptcha/verify (:g-recaptcha-response payload) recaptcha-threshold recaptcha-secret)]
      (if verified
        (.send res add-comment-response)
        (.send res "Uh oh. Recaptcha failed. Are you a robot?")))))

(defn get-comments-form-handler
  [{:keys [htmx-config]}]
  (fn [req res]
    (let [{:keys [post-id]} (extract-params [{:type :query :name "post-id" :required true}] req)
          comments-form-html (htmx/get-comments-form-html htmx-config post-id)]
      (.send res comments-form-html))))

(defn make-express-config
  [user-config]
  (let [defaults {:allowed-origin-url "http://localhost:8080"
                  :recaptcha-threshold 0.5}]
    (merge defaults user-config)))

(defn create-app
  [{:keys [allowed-origin-url] :as config}]
  (let [app (express)]
    (.use app (.urlencoded express #js {:extended true}))
    (.use app (fn [_ res next]
                (doto res
                  (.set "Access-Control-Allow-Origin" allowed-origin-url)
                  (.set "Access-Control-Allow-Methods" "GET, POST")
                  (.set "Access-Control-Allow-Headers" "hx-trigger, hx-target, hx-request, hx-current-url"))
                (next)))

    (.get app "/comments" (get-comments-handler config))
    (.post app "/comments" (post-comment-handler config))
    (.get app "/comments-form" (get-comments-form-handler config))

    app))

(defn start-server
  [app & {:keys [port callback]
          :or {port 3000
               callback (fn [] (.log js/console "Listening on port 3000!"))}}]
  (let [server (.createServer http app)]
    (.listen server port callback)))

(defn stop-server
  [server]
  (.close server))

(comment
  (def example-req #js {:body #js {:post-id "foo-bar"}})

  (aget example-req "body")
  ((str ".-" "body"))

  (def example-param {:body "author" :required true})
  (str example-param)

  (def ks [:foo :bar :baz])
  (def vs [1 2 3])
  (into {} (map vector ks vs))

  (def example #js {:body #js {:author "Nicholas"}})
  (extract-param {:type :body :name "author" :required true} example)
  (extract-param {:type :body :name "missing" :required true} example)
  (extract-param {:type :body :name "missing" :required false} example)

  (extract-param {:type :query :name "pid" :required true} #js {:query #js {:pid "foo"}})

  (gobj/getValueByKeys example-req "body" "post-id")
  (gobj/getValueByKeys example-req "query" "post-id")

  (def example-req #js {:body #js {:author "nick"
                                   :message "hiya"
                                   :post-id "foo-bar"}})
  (def expected {:author "nick" :message "hiya" :post-id "foo-bar"})
  (assert (= expected
             (extract-params
              [{:type :body :name "author" :required true}
               {:type :body :name "message" :required true}
               {:type :body :name "post-id" :required true}]
              example-req)))

  ; Make sure it throws an error when missing required param
  (assert
    (instance?
      js/Error
      (try
        (extract-params
          [{:type :body :name "missing" :required true}
           {:type :body :name "message" :required true}
           {:type :body :name "post-id" :required true}]
          example-req)
        (catch js/Error err err)))))
