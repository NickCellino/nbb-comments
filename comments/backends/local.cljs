(ns comments.backends.local)

(def comments (atom []))

(defn add-comment
  [new-comment]
  (swap!
    comments
    (fn [current-comments]
      (conj current-comments new-comment))))

(defn list-comments
  [post-id]
  (filter #(= (:post-id %) post-id) @comments))

(comment
  @comments
  (add-comment {:post-id "clojure-bandits" :message "Great post!" :time "12345" :author "Nick"})
  (add-comment {:post-id "clojure-bandits" :message "This post was ight" :time "999" :author "Jeremy"})
  (add-comment {:post-id "foo" :message "cool post!"})
  (list-comments "asdf")
  (list-comments "foo"))
