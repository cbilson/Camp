;; -*- compilation-command: "msbuild /t:CampExe /verbosity:minimal"
(ns camp.tasks.new
  (:require [clojure.clr.io :as io]
            [camp.core :refer [getenv]]
            [camp.io :as cio]))

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
  (println  "(defproject " name "\"0.1.0-SNAPSHOT\"")
  (println  "  :description \"TODO: describe\"")
  (println  "  :license {:name \"BSD\"")
  (println  "            :url \"http://www.opensource.org/licenses/BSD-3-Clause\"")
  (println  "            :distribution :repo}")
  (println  "  :dependencies [[Clojure \"1.6.0.1\"]]})"))

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
