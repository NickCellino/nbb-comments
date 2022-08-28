; htmx interface for comments backend
(ns htmx
  (:require [html-serializer :as html]
            [hiccup]))

(defn get-comments-factory
  "Retrieves a list of comments as HTML."
  [get-comments]
  (fn [post-id]
    (let [cmts (get-comments post-id)
          html-comments (html/serialize-comment-list cmts)]
      (hiccup/html html-comments))))

(defn post-comment-factory
  "Adds a comment. Returns HTML representing
  the added comment and also the new, blank input form
  to render in place of the old one."
  [gen-comments-form save-comment]
  (fn
    [cmt]
    (let [comment-time (.toISOString (js/Date.))
          cmt-w-time (assoc cmt :time comment-time)
          comment-result (save-comment cmt-w-time)
          serialized-result (html/serialize-comment comment-result)
          new-comment-form (gen-comments-form (:post-id cmt))
          result (list new-comment-form serialized-result)]
      (hiccup/html result))))

(defn comments-form-factory
  [comment-form-id comment-list-div-id post-url]
  (fn [post-id]
    [:form
     {:id comment-form-id
      :hx-post post-url
      :hx-swap "afterbegin"
      :hx-target (str "#" comment-list-div-id)
      :hx-swap-oob "true"}
     [:input {:type "hidden" :name "post-id" :value post-id}]
     [:label {:for "author"} "Name (optional)"]
     [:input {:type "text" :name "author"}]
     [:label {:for "message"} "Comment"]
     [:textarea {:name "message" :required true :rows 5}]
     [:button {:type "submit"} "Submit"]]))

(defn configure-htmx
  [{:keys [comment-form-id
           comment-list-div-id
           post-comment-url
           save-comment-fn
           get-comments-fn]}]
  (let [gen-comments-form (comments-form-factory comment-form-id comment-list-div-id post-comment-url)
        gen-comments-form-html (comp hiccup/html gen-comments-form)
        post-comment (post-comment-factory gen-comments-form save-comment-fn)
        get-comments (get-comments-factory get-comments-fn)]
    {:gen-comments-form gen-comments-form
     :gen-comments-form-html gen-comments-form-html
     :post-comment post-comment
     :get-comments get-comments}))

(comment
  (defn mock-save-cmt [cmt] (println "saving comment!" cmt) cmt)

  (defn mock-get-cmts
    [_]
    [{:author "Nick" :message "hello world!" :time "2020-08-20T12:03:04Z"}
     {:author "Joe" :message "hello world!" :time "2020-08-20T12:03:04Z"}
     {:author "Mike" :message "hello world!" :time "2020-08-20T12:03:04Z"}
     {:author "Alex" :message "hello world!" :time "2020-08-20T12:03:04Z"}])

  (def htmx-backend (configure-htmx {:comment-form-id "comments-form"
                                     :comment-list-div-id "comments-list"
                                     :post-comment-url "http://mock.com/foo"
                                     :save-comment-fn mock-save-cmt
                                     :get-comments-fn mock-get-cmts}))

  (def post-comment (:post-comment htmx-backend))
  (post-comment {:author "Nick" :message "hello world!" :time "2020-08-20T12:03:04Z"})

  ((:get-comments htmx-backend) "asdf"))
