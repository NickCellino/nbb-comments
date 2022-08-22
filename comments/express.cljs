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

(defn create-app
  [add-comment list-comments]
  (let [app (express)]
    (.use app (.urlencoded express #js {:extended true}))

    (.get app "/comments" (fn [req res]
                            (let [comments (list-comments "clojure-bandits")]
                              (.send res (hiccup/html (html/serialize-comment-list comments))))))

    (.post app "/comments" (fn [req res]
                             (let [comment-input (parse-comment-body (.-body req))
                                   post-id (:post-id comment-input)]
                               (add-comment comment-input)
                               (.send res (hiccup/html (list (html/comments-form post-id)
                                                             (html/serialize-comment comment-input)))))))

    (.get app "/comments-form" (fn [req res]
                                 (let [post-id (.-post-id (.-query req))]
                                   (if (nil? post-id)
                                     (-> res
                                         (.status 400)
                                         (.send "post-id query param is required."))
                                     (.send res (hiccup/html (html/comments-form post-id)))))))
    app))

(defn start-server
  [app port callback]
  (let [server (.createServer http app)]
    (.listen server port callback)))

(defn stop-server
  [server]
  (.close server))
