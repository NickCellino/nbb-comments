(ns comments.html
  (:require [clojure.string :as string]))

(conj [] nil)

(or nil "world")

; TODO escape html
(defn get-author-html
  [comment-body]
  (let [author-name (or (:author comment-body) "Anonymous")]
    (str "<p class=\"name\"><strong>" author-name "</strong> said...</p>")))

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
 
