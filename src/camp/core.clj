(ns camp.core
  (:require [clojure.edn :as edn]
            [clojure.clr.io :as io]
            [camp.io :as cio])
  (:import [System Environment]
           [System.IO File]
           [clojure.lang PushbackTextReader]))

(defn resolve-task
  "Given the name of a task, find the function that implements it."
  [task-name]
  (let [ns (str "camp.tasks." task-name)
        ns-sym (symbol ns)
        sym (symbol ns task-name)]
    (when-not (find-ns ns-sym)
      (require ns-sym))
    (resolve sym)))

(defn read-project
  "Read  the project file."
  ([] (read-project "project.clj"))
  ([project-file-name]
   (if (cio/file-exists? project-file-name)
     (with-open [reader (PushbackTextReader. (File/OpenText project-file-name))]
       (edn/read reader))
     {})))

(defn getenv
  "Get the value of an environment variable"
  ([name] (getenv name nil))
  ([name default-value]
   (or (Environment/GetEnvironmentVariable name)
       default-value)))
