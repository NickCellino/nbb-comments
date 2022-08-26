(ns comments.html
  (:require [clojure.string :as string]
            [comments.hiccup-clone :refer [html]]))
"
const formatDate = (date) => {
  const months = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August',
    'September', 'October', 'November', 'December'];

  const month = months[date.getMonth()];
  const year = 1900 + date.getYear();

  return `${date.getHours()}:${date.getMinutes()}, ${month} ${date.getDate()}, ${year}`
}
"
(defn format-date
  [iso-date]
  (let [date-obj (js/Date. iso-date)
        months ["January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"]
        date-month (get months (.getMonth date-obj))
        date-year (+ 1900 (.getYear date-obj))]
    (str (.getHours date-obj) ":" (.getMinutes date-obj) ", " date-month " " (.getDate date-obj) ", " date-year))) 

(defn serialize-comment
  [comment-body]
  (let [author-section [:p {:class "name"} [:strong (or (:author comment-body) "Anonymous")] "said..."]
        message-section [:p {:class "message"} (:message comment-body)]
        date-section [:p {:class "datetime"} (format-date (:time comment-body))]]
    [:div {:class "comment"} author-section message-section date-section]))

(defn serialize-comment-list
  [comments]
  (let [serialized-comments (map serialize-comment comments)]
    [:div {:id "comment-list"} serialized-comments]))
 
(defn comments-form
  [post-id post-url]
  [:form
   {:id "comment-form" :hx-post post-url :hx-swap "afterbegin" :hx-target "#comment-list" :hx-swap-oob "true"}
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
