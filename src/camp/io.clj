(ns camp.io
  (:import [System.IO Path File Directory]
           [clojure.lang PushbackTextReader]))

(defn mkdir
  "Create a directory."
  ([name]
   (Directory/CreateDirectory (str name)))
  ([name access-control]
   (Directory/CreateDirectory (str name) access-control)))

(defn directory-exists?
  "Check to see if a directory exists."
  [name]
  (Directory/Exists name))

(defn file-exists?
  "Check to see if a file exists."
  [name]
  (File/Exists name))

(defn file
  "TODO: pull request to clojure-clr? Need approval from work, sign contributor
  agreement, ..., friction."
  [& args]
  (Path/Combine (->> args (map str) to-array (into-array String))))

(defn reader
  "clojure.clr.io only has text-reader, which isn't a PushbackTextReader
  for some reason."
  [path]
  (PushbackTextReader. (File/OpenText path)))
