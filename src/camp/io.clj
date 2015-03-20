(ns camp.io
  "Functions for doing IO."
  (:import [System.IO Path File Directory SearchOption]
           [clojure.lang PushbackTextReader]))

(defn mkdir
  "Create a directory."
  ([name]
   (Directory/CreateDirectory (str name)))
  ([name access-control]
   (Directory/CreateDirectory (str name) access-control)))

(defn files
  "Get all files in some path."
  ([base-path]
   (Directory/GetFiles base-path))
  ([base-path pattern]
   (Directory/GetFiles base-path pattern))
  ([base-path pattern option]
   (Directory/GetFiles base-path pattern (enum-val SearchOption option))))

(defn directory-exists?
  "Check to see if a directory exists."
  [name]
  (Directory/Exists name))

(defn current-directory []
  (Directory/GetCurrentDirectory))

(defn set-current-directory [path]
  (Directory/SetCurrentDirectory path))

(defmacro with-current-directory
  "Do something with current directory set to some value then restore it."
  [path & forms]
  `(let [original# (current-directory)]
    (set-current-directory ~path)
    (try
      ~@forms
      (finally (set-current-directory original#)))))

(defn file-exists?
  "Check to see if a file exists."
  [name]
  (File/Exists name))

(defn newer?
  "Determine if one file, a, is newer than another file, b."
  [a b]
  (> (DateTime/Compare (File/GetLastWriteTimeUtc a)
                       (File/GetLastWriteTimeUtc b))
     0))

(defn file
  "Format a file name. Should be in clojure.clr.io."
  [& args]
  (Path/Combine (->> args (map str) to-array (into-array String))))

(defn reader
  "clojure.clr.io only has text-reader, which isn't a PushbackTextReader
  for some reason."
  [path]
  (PushbackTextReader. (File/OpenText path)))

(def directory-separator (str (Path/DirectorySeparatorChar)))

(def path-separator (str (Path/PathSeparator)))

(defn file-name-only [file-name]
  (Path/GetFileName file-name))

(defn directory [file-name]
  (Path/GetDirectoryName file-name))

(defn file-name-without-extension [file-name]
  (Path/GetFileNameWithoutExtension file-name))

(defn copy
  ([src dst]
   (File/Copy src dst))
  ([src dst overwrite?]
   (File/Copy src dst overwrite?)))

(defn resolve-path [path]
  (Path/GetFullPath path))

(defn rooted? [path]
  (Path/IsPathRooted path))
