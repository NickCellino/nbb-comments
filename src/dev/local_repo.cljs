(ns dev.local-repo
  (:require [repo]))

(def comments (atom []))

(defn add-comment
  [new-comment]
  (swap!
    comments
    (fn [current-comments]
      (conj current-comments new-comment)))
  new-comment)

(defmethod repo/save-comment :local
  [_ new-comment]
  (add-comment new-comment))

(defn list-comments
  [post-id]
  (reverse (filter #(= (:post-id %) post-id) @comments)))

(defmethod repo/get-comments :local
  [_ post-id]
  (list-comments post-id))

(comment
  @comments
  (add-comment {:post-id "clojure-bandits" :message "Great post!" :time "12345" :author "Nick"})
  (add-comment {:post-id "clojure-bandits" :message "This post was ight" :time "999" :author "Jeremy"})
  (add-comment {:post-id "foo" :message "cool post!"})
  (list-comments "clojure-bandits")
  (list-comments "test-post")
  (list-comments "foo")

  (swap!
    comments
    (fn [current-comments]
      (conj current-comments {:post-id "test-post" :message "Great post!" :time "12345" :author "Junior Soprano"}))))


