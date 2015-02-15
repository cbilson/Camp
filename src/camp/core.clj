(ns camp.core
  (:require [clojure.edn :as edn]
            [clojure.clr.io :as io]
            [camp.io :as cio])
  (:import [System Environment]))

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
  []
  (if (cio/file-exists? "project.clj")
    (with-open [reader (io/text-reader "project.clj")]
      (edn/read reader))
    {}))

(defn getenv
  "Get the value of an environment variable"
  ([name] (getenv name nil))
  ([name default-value]
   (or (Environment/GetEnvironmentVariable name)
       default-value)))
