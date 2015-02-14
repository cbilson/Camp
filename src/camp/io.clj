(ns camp.io
  (:import [System.IO Path File Directory]))

(defn mkdir
  "Create a directory."
  ([name]
   (Directory/CreateDirectory (str name)))
  ([name access-control]
   (Directory/CreateDirectory (str name) access-control)))

(defn file
  "TODO: pull request to clojure-clr? Need approval from work, sign contributor
  agreement, ..., friction."
  [& args]
  (Path/Combine (->> args (map str) to-array (into-array String))))
