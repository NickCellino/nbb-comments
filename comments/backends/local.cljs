(ns comments.backends.local)

(def comments (atom []))

(defn add-comment
  [post-id message time author]
  (let [new-comment (cond-> {}
                      post-id (assoc :post-id post-id)
                      message (assoc :message message)
                      time (assoc :time time)
                      author (assoc :author author))]
    (swap!
      comments
      (fn [current-comments]
        (conj current-comments new-comment)))))

(defn list-comments
  [post-id]
  (filter #(= (:post-id %) post-id) @comments))

(comment
  (add-comment "clojure-bandits" "Great post!" "12345" "Nick")
  (add-comment "foo" "Great post!" nil nil)
  (list-comments "asdf"))
