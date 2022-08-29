; htmx interface for comments backend
(ns htmx
  (:require [html-serializer :as html]
            [hiccup]
            [promesa.core :as p]))

(defn get-comments-factory
  "Retrieves a list of comments as HTML."
  [get-comments]
  (fn [post-id]
    (p/let [cmts (get-comments post-id)
            html-comments (html/serialize-comment-list cmts)]
      (hiccup/html html-comments))))

(defn author-input
  ([id swap-oob]
   (let [html-attrs {:type "text" :name "author" :id id}
         html-attrs (if swap-oob (assoc html-attrs :hx-swap-oob true) html-attrs)]
     [:input html-attrs]))
  ([swap-oob] (author-input "author-input" swap-oob)))

(defn message-input
  ([id swap-oob]
   (let [html-attrs {:name "message" :required true :rows 5 :id id}
         html-attrs (if swap-oob (assoc html-attrs :hx-swap-oob true) html-attrs)]
     [:textarea html-attrs]))
  ([swap-oob] (message-input "message-input" swap-oob)))

(defn post-comment-factory
  "Adds a comment. Returns HTML representing
  the added comment and also the new, blank input form
  to render in place of the old one."
  [save-comment]
  (fn
    [cmt]
    (p/let [comment-time (.toISOString (js/Date.))
            cmt-w-time (assoc cmt :time comment-time)
            comment-result (save-comment cmt-w-time)
            serialized-result (html/serialize-comment comment-result)]
      (str
        (hiccup/html (list
                       (author-input true)
                       (message-input true)))
        "\n"
        (hiccup/html serialized-result)))))

(defn get-submit-comment-button
  [recaptcha-sitekey]
  (if recaptcha-sitekey
    [:button {:type "submit"
              :class "g-recaptcha"
              :data-sitekey recaptcha-sitekey
              :data-callback "announce"} "Submit"]
    [:button {:type "submit"} "Submit"]))

(defn get-recaptcha-callback-js
  [comment-form-id recaptcha-callback-event]
  (str
    "function announce(token) {\n"
    "  const event = new Event('" recaptcha-callback-event "');\n"
    "  const elem = document.querySelector('#" comment-form-id "');\n"
    "  elem.dispatchEvent(event);\n"
    "}"))

(defn comments-form-factory
  [comment-form-id comment-list-div-id post-url submit-comment-button recaptcha-callback-event recaptcha-callback-js]
  (fn [post-id]
    [:form
     {:id comment-form-id
      :hx-post post-url
      :hx-swap "afterbegin"
      :hx-target (str "#" comment-list-div-id)
      :hx-trigger recaptcha-callback-event
      :hx-swap-oob "true"}
     [:input {:type "hidden" :name "post-id" :value post-id}]
     [:label {:for "author"} "Name (optional)"]
     (author-input false)
     [:label {:for "message"} "Comment"]
     (message-input false)
     [:script recaptcha-callback-js]
     submit-comment-button]))

(defn configure-htmx
  [{:keys [comment-form-id
           comment-list-div-id
           post-comment-url
           save-comment-fn
           get-comments-fn
           recaptcha-sitekey]}]
  (let [submit-comment-button (get-submit-comment-button recaptcha-sitekey)
        recaptcha-callback-event "recaptcha-verified"
        recaptcha-callback-js (get-recaptcha-callback-js comment-form-id recaptcha-callback-event)
        gen-comments-form (comments-form-factory comment-form-id
                                                 comment-list-div-id
                                                 post-comment-url
                                                 submit-comment-button
                                                 recaptcha-callback-event
                                                 recaptcha-callback-js)
        gen-comments-form-html (comp #(hiccup/html % true) gen-comments-form)
        post-comment (post-comment-factory save-comment-fn)
        get-comments (get-comments-factory get-comments-fn)]
    {:gen-comments-form gen-comments-form
     :gen-comments-form-html gen-comments-form-html
     :post-comment post-comment
     :get-comments get-comments
     :submit-comment-button submit-comment-button}))

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
