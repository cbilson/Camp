(ns camp.core
  (:require [clojure.edn :as edn]
            [clojure.clr.io :as io]
            [camp.io :as cio])
  (:import [System Environment]
           [System.IO File]
           [clojure.lang PushbackTextReader]))

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

(def ^:dynamic *options*
  {:error? true
   :warn? true
   :info? true
   :verbose? false
   :debug? false})

(defn print-when [flag messages]
  (when (*options* flag) (apply println messages)))

(defn verbose [& messages]
  (print-when :verbose? messages))

(defn info [& messages]
  (print-when :info? messages))

(defn warn [& messages]
  (print-when :warn? messages))

(defn error [& messages]
  (print-when :error? messages))

(defn to-dictionary [m]
  (let [d (|System.Collections.Generic.Dictionary`2[System.String,System.String]|.)]
    (doseq [k (keys m)]
      (.Add d (str k) (str (m k))))))

(defn resolve-task
  "Given the name of a task, find the function that implements it."
  [task-name]
  (try
    (let [ns (str "camp.tasks." task-name)
          ns-sym (symbol ns)
          sym (symbol ns task-name)]
      (when-not (find-ns ns-sym)
        (require ns-sym))
      (resolve sym))
    (catch Exception e
      (error "Task" task-name "not found.")
      (let [ns-sym (symbol "camp.tasks.help")]
        (require ns-sym)
        (resolve (symbol ns-sym "help"))))))
