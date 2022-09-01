(ns repo)

(defmulti get-comments :repo)
(defmulti save-comment :repo)

