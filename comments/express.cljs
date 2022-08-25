(ns comments.express
  (:require ["express$default" :as express]
            ["http" :as http]
            [cljs-bean.core :as bean]
            [comments.hiccup-clone :as hiccup]
            [comments.html :as html]))

(defn parse-comment-body
  [js-body]
  (let [body (bean/->clj js-body)]
    (cond-> {}
      (:author body) (assoc :author (:author body))
      (:message body) (assoc :message (:message body))
      (:post-id body) (assoc :post-id (:post-id body)))))

(defn get-comments-fn [list-comments]
  (fn [req res]
    (let [post-id (.-post-id (.-query req))
          comments (list-comments post-id)]
      (.send res (hiccup/html (html/serialize-comment-list comments))))))

(defn add-comment-fn [add-comment add-comment-url]
  (fn [req res]
    (let [comment-input (parse-comment-body (.-body req))
          post-id (:post-id comment-input)]
      (add-comment comment-input)
      (.send res (hiccup/html (list (html/comments-form post-id add-comment-url)
                                    (html/serialize-comment comment-input)))))))

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
