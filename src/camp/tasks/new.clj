;; -*- compilation-command: "msbuild /t:CampExe /verbosity:minimal"
(ns camp.tasks.new
  (:require [clojure.clr.io :as io]
            [clojure.pprint :refer [pprint with-pprint-dispatch code-dispatch]]
            [camp.core :refer [getenv]]
            [camp.io :as cio]))

(defn pprint-form [form]
  (with-pprint-dispatch code-dispatch
    (pprint form)))

(defn gitignore []
  (println "/targets")
  (println "/checkouts")
  (println "/packages")
  (println "*.dll")
  (println "/src/**/*.exe")
  (println "/.nrepl-port")
  (println "/.repl")
  (println "/out"))

(defn project-clj [name]
  (pprint-form
   `(defproject ~name "0.1.0-SNAPSHOT"
      :depends [[Clojure "1.6.0.1"]]
      :plugins [['not-supported-yet]])))

(defn write-template [fname template-fn & args]
  (with-open [writer (io/text-writer fname)]
    (binding [*out* writer]
      (apply template-fn args))))

(defn new
  "Create a new ClojureCLR project from a template."
  [{:keys [name] :as project} & args]
  (cio/mkdir name)
  (write-template (cio/file (str name) "project.clj") project-clj name)
  (write-template (cio/file (str name) ".gitignore") gitignore))
