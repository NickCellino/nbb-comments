(ns express
  (:require ["express$default" :as express]
            ["http" :as http]
            [promesa.core :as p]
            [goog.object :as gobj]))

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
      req"
  [params req]
  (let [values (map #(extract-param % req) params)
        param-kw-names (map (comp keyword :name) params)]
    (into {} (map vector param-kw-names values))))

(defn get-comments-handler-factory
  [list-comments]
  (fn [req res]
    (p/let [{:keys [post-id]} (extract-params [{:type :query :name "post-id" :required true}] req)
            list-comments-response (list-comments post-id)]
      (.send res list-comments-response))))

(defn post-comment-handler-factory
  [add-comment]
  (fn [req res]
    (p/let [payload-config [{:type :body :name "author" :required true}
                            {:type :body :name "message" :required true}
                            {:type :body :name "post-id" :required true}]
            cmt (extract-params payload-config req)
            add-comment-response (add-comment cmt)]
      (.send res add-comment-response))))

(defn get-comments-form-handler-factory
  [gen-comments-form-html]
  (fn [req res]
    (let [{:keys [post-id]} (extract-params [{:type :query :name "post-id" :required true}] req)
          comments-form-html (gen-comments-form-html post-id)]
      (.send res comments-form-html))))

(defn create-app
  [post-comment-handler get-comments-handler get-comments-form-handler allowed-origin-url]
  (let [app (express)]
    (.use app (.urlencoded express #js {:extended true}))
    (.use app (fn [_ res next]
                (doto res
                  (.set "Access-Control-Allow-Origin" allowed-origin-url)
                  (.set "Access-Control-Allow-Methods" "GET, POST")
                  (.set "Access-Control-Allow-Headers" "hx-trigger, hx-target, hx-request, hx-current-url"))
                (next)))

    (.get app "/comments" get-comments-handler)
    (.post app "/comments" post-comment-handler)

    (.get app "/comments-form" get-comments-form-handler)

    app))

(defn start-server
  [app port callback]
  (let [server (.createServer http app)]
    (.listen server port callback)))

(defn stop-server
  [server]
  (.close server))

(defn configure-express
  [{:keys [post-comment
           get-comments
           gen-comments-form-html
           allowed-origin-url]}]
  (let [post-comment-handler (post-comment-handler-factory post-comment)
        get-comments-handler (get-comments-handler-factory get-comments)
        get-comments-form-handler (get-comments-form-handler-factory gen-comments-form-html)
        app (create-app post-comment-handler get-comments-handler get-comments-form-handler allowed-origin-url)]
    {:app app}))

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
