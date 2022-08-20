(ns app
  (:require ["@aws-sdk/client-s3" :as s3]
            [promesa.core :as p]))


(defn fetch-file
  [bucket key]
  (let [s3-client (s3/S3Client. {:region "us-east-1"})
        get-obj-cmd (s3/GetObjectCommand. #js {:Bucket bucket :Key key})
        response (.send s3-client get-obj-cmd)]
    response))

(defn stream-to-string
  [fstream]
  (js/Promise.
    (fn [resolve]
      (let [chunks #js []]
        (.on fstream "data" (fn [chunk] (.push chunks chunk)))
        (.on fstream "end" (fn [] (resolve (.toString (.concat js/Buffer chunks) "utf8"))))))))
 
(.log js/console "hello")

(defn main []
  (.log js/console s3/S3Client)
  (p/let [s3-response (fetch-file "olv-static" "olv.png")]
    (.log js/console (.-Body s3-response))))

(main)

