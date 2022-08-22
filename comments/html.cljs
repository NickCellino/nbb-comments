(ns comments.html
  (:require [clojure.string :as string]
            [comments.hiccup-clone :refer [html]]))

(defn serialize-comment
  [comment-body]
  (let [author-section [:p {:class "name"} [:strong (or (:author comment-body) "Anonymous")] "said..."]
        message-section [:p {:class "message"} (:message comment-body)]]
    [:div {:class "comment"} author-section message-section]))

(defn serialize-comment-list
  [comments]
  (let [serialized-comments (map serialize-comment comments)]
    [:div {:id "comment-list"} serialized-comments]))
 
(defn comments-form
  [post-id]
  [:form
   {:id "comment-form" :hx-post "http://localhost:3000/comments" :hx-swap "afterbegin" :hx-target "#comment-list" :hx-swap-oob "true"}
   [:input {:type "hidden" :name "post-id" :value post-id}]
   [:label {:for "author"} "Name (optional)"]
   [:input {:type "text" :name "author"}]
   [:label {:for "message"} "Comment"]
   [:textarea {:name "message" :required true :rows 5}]
   [:button {:type "submit"} "Submit"]])

(comment
  (html [:p {:class "name"} "<div>hello world</div>"])
  (html [:p {:class "name"} [:strong "nick"] "said..."])

  (html (serialize-comment {:author "Nicholas" :message "hello world!"}))
  (html (serialize-comment {:message "hello world!"}))
  (html (serialize-comment {:message "<script>alert('you got pwned')</script>"}))

  (html (serialize-comment-list [{:author "Nicholas" :message "hello world!"}
                                 {:message "hello world!"}
                                 {:message "yo"}
                                 {:message "sup"}]))

  (html (comments-form "clojure-bandits")))
