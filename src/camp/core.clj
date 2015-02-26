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

(defn getenv
  "Get the value of an environment variable"
  ([name] (getenv name nil))
  ([name default-value]
   (or (Environment/GetEnvironmentVariable name)
       default-value)))

(defn setenv
  "Sets an environment variable's value."
  ([name value]
   (Environment/SetEnvironmentVariable name value)))

(defn print-when [flag messages]
  (when flag (apply println messages)))

(defn verbose [{verbose? :verbose?} & messages]
  (print-when verbose? messages))

(defn info [{info? :info?} & messages]
  (print-when info? messages))

(defn warn [{warn? :warn?} & messages]
  (print-when warn? messages))

(defn error [{error? :error?} & messages]
  (print-when error? messages))
