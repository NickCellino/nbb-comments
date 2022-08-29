; mostly copied from borkdude comment in clojurians#nbb slack channel
(ns hiccup
  (:require [clojure.string :as str]
            [goog.string :as gstr]))

(defn escape-html
  [text]
  (-> text
    (str/replace "&" "&amp;")
    (str/replace "<" "&lt;")
    (str/replace ">" "&gt;")
    (str/replace "\"" "&quot;")
    (str/replace "'" "&apos;")))

(def foo (with-meta {} {:raw true}))
(meta foo)

(defn html
  ([v skip-escape-html]
   (cond (vector? v)
         (let [tag (first v)
               attrs (second v)
               attrs (when (map? attrs) attrs)
               elts (if attrs (nnext v) (next v))
               tag-name (name tag)]
           (gstr/format
             "<%s%s>%s</%s>\n"
             tag-name
             (html attrs skip-escape-html)
             (html elts skip-escape-html)
             tag-name))
         (map? v)
         (str/join ""
                   (map (fn [[k v]]
                          (gstr/format " %s=\"%s\"" (name k) v)) v))
         (seq? v)
         (str/join " " (map #(html % skip-escape-html) v))
         :else (if skip-escape-html (str v) (escape-html (str v)))))
  ([v]
   (html v false)))

