(ns camp.core
  (:require [clojure.clr.io :as io]
            [clojure.string :as str]
            [camp.io :as cio])
  (:import [System Environment]
           [System.IO File]
           [clojure.lang PushbackTextReader]))

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

(defn env
  "Get or set the value of an environment variable."
  ([name]
   (let [val (Environment/GetEnvironmentVariable name)]
     (debug "env" name "->" (pr-str val))))
  ([name value]
   (debug "setenv" name "=" (pr-str value))
   (Environment/SetEnvironmentVariable name value)))

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
