(ns htmx)
; htmx interface over a comments backend

(defn get-comments
  "Retrieves a list of comments as HTML."
  [post-id]
  ; TODO
  post-id)

(defn post-comment-factory
  [gen-comments-form]
  (fn
    "Adds a comment. Returns HTML representing
    the added comment and also the new, blank input form
    to render in place of the old one."
    [cmt]
    ; TODO
    cmt))

(defn comments-form-factory
  [comment-form-id comment-list-div-id]
  (fn [post-id post-url]
    [:form
     {:id comment-form-id :hx-post post-url :hx-swap "afterbegin" :hx-target comment-list-div-id :hx-swap-oob "true"}
     [:input {:type "hidden" :name "post-id" :value post-id}]
     [:label {:for "author"} "Name (optional)"]
     [:input {:type "text" :name "author"}]
     [:label {:for "message"} "Comment"]
     [:textarea {:name "message" :required true :rows 5}]
     [:button {:type "submit"} "Submit"]]))

(defn configure-htmx
  [comment-form-id comment-list-div-id]
  (let [gen-comments-form (comments-form-factory comment-form-id comment-list-div-id)
        post-comment (post-comment-factory gen-comments-form)]
    {:gen-comments-form gen-comments-form
     :post-comment post-comment}))
