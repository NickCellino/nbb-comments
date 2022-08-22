; mostly copied from borkdude comment in clojurians#nbb slack channel
(ns comments.hiccup-clone
  (:require [clojure.string :as str]
            [goog.string :as gstr]
            [cljs-bean.core :as bean]))

(defn escape-html
  [text]
  (-> text
    (str/replace "&" "&amp;")
    (str/replace "<" "&lt;")
    (str/replace ">" "&gt;")
    (str/replace "\"" "&quot;")
    (str/replace "'" "&apos;")))

(defn html [v]
  (cond (vector? v)
        (let [tag (first v)
              attrs (second v)
              attrs (when (map? attrs) attrs)
              elts (if attrs (nnext v) (next v))
              tag-name (name tag)]
          (gstr/format "<%s%s>%s</%s>\n" tag-name (html attrs) (html elts) tag-name))
        (map? v)
        (str/join ""
                  (map (fn [[k v]]
                         (gstr/format " %s=\"%s\"" (name k) v)) v))
        (seq? v)
        (str/join " " (map html v))
        :else (escape-html (str v))))

