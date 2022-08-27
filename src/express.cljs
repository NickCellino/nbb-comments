(ns express
  (:require ["express$default" :as express]
            ["http" :as http]
            [cljs-bean.core :as bean]
            [hiccup :as hiccup]
            [html :as html]
            [promesa.core :as p]
            [goog.object :as gobj]))

(defn extract-param
  [param req]
  (let [param-type (:type param)
        val (cond
              (= param-type :body) (gobj/getValueByKeys req "body" (:name param))
              :else nil)]
    (when (and (nil? val) (:required param))
      (throw (js/Error. (str "missing required parameter " param))))
    val))

(defn extract-params
  [params req]
  (let [values (map #(extract-param % req) params)
        param-kw-names (map (comp keyword :name) params)]
    (into {} (map vector param-kw-names values))))

(defn parse-comment-body
  [js-body]
  (let [body (bean/->clj js-body)]
    (cond-> {}
      (:author body) (assoc :author (:author body))
      (:message body) (assoc :message (:message body))
      (:post-id body) (assoc :post-id (:post-id body)))))

(defn get-comments-fn [list-comments]
  (fn [req res]
    (p/let [post-id (.-post-id (.-query req))
            comments (list-comments post-id)]
      (.send res (hiccup/html (html/serialize-comment-list comments))))))

(defn add-comment-fn [add-comment add-comment-url]
  (fn [req res]
    (p/let [comment-input (parse-comment-body (.-body req))
            post-id (:post-id comment-input)]
      (add-comment comment-input)
      (.send res (hiccup/html (list (html/comments-form post-id add-comment-url)
                                    (html/serialize-comment (assoc comment-input :time (.toISOString (js/Date.))))))))))

(defn get-comments-form-fn [add-comment-url]
  (fn [req res]
    (let [post-id (.-post-id (.-query req))]
      (if (nil? post-id)
        (-> res
            (.status 400)
            (.send "post-id query param is required."))
        (.send res (hiccup/html (html/comments-form post-id add-comment-url)))))))

(defn create-app
  [add-comment list-comments add-comment-url frontend-url]
  (let [app (express)]
    (.use app (.urlencoded express #js {:extended true}))
    (.use app (fn [_ res next]
                (doto res
                  (.set "Access-Control-Allow-Origin" frontend-url)
                  (.set "Access-Control-Allow-Methods" "GET, POST")
                  (.set "Access-Control-Allow-Headers" "hx-trigger, hx-target, hx-request, hx-current-url"))
                (next)))
    (.use app (fn [req _ next]
                (.log js/console "got request" req)
                (next)))

    (.get app "/comments" (get-comments-fn list-comments))
    (.post app "/comments" (add-comment-fn add-comment add-comment-url))

    (.get app "/comments-form" (get-comments-form-fn add-comment-url))

    app))

(defn start-server
  [app port callback]
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
