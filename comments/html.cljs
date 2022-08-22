(ns comments.html
  (:require [clojure.string :as string]
            [comments.hiccup-clone :refer [html]]))

(html [:p {:class "name"} "<div>hello world</div>"])

(html [:p {:class "name"} [:strong "nick"] "said..."])

; TODO escape html
(defn get-author-html
  [comment-body]
  (let [author-name (or (:author comment-body) "Anonymous")]
    (html [:p {:class "name"} [:strong author-name] "said..."])))

(defn serialize-comment
  [comment-body]
  (let [header "<div class=\"comment\">"
        author-section (str "\t" (get-author-html comment-body))
        message-section (str "\t<p class=\"message\">" (:message comment-body) "</p>")
        footer "</div>"]
    (string/join "\n" [header author-section message-section footer])))

(defn serialize-comment-list
  [comments]
  (let [serialized-comments (map serialize-comment comments)]
    (string/join "\n" serialized-comments)))
 
(def comments-form
  (str
    "<form id=\"comment-form\" hx-post=\"/comments\" hx-swap=\"afterbegin\" hx-target=\"#comments\" hx-swap-oob=\"true\">"
    "\t<label for=\"name\">Name (optional)</label>"
    "\t<input type=\"text\" name=\"name\">"
    "\t<label for=\"message\">Message</label>"
    "\t<textarea name=\"message\" required rows=5></textarea>"
    "\t<button type=\"submit\">submit</button>"
    "</form>"))
