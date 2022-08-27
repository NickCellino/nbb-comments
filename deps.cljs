(ns deps
  (:require [nbb.classpath :refer [add-classpath get-classpath]]))

(add-classpath "./src")
(println (get-classpath))
