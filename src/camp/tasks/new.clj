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
  (println "/out")
  (println "")
  (println "# ignore emacs temp files")
  (println "\\#*"))

(defn project-clj [name]
  (println  "(defproject" name "\"0.1.0-SNAPSHOT\"")
  (println  "  :description \"TODO: describe\"")
  (println  "  :license {:name \"BSD\"")
  (println  "            :url \"http://www.opensource.org/licenses/BSD-3-Clause\"")
  (println  "            :distribution :repo}")
  (println  "  :dependencies [[Clojure \"1.6.0.1\"]])"))

(defn core-clj [name]
  (println "(ns" name)
  (println "  (:gen-class))")
  (println "")
  (println "(defn -main [& args]")
  (println "  (println \"TODO: something\")"))

(defn write-template [fname template-fn & args]
  (with-open [writer (io/text-writer fname)]
    (binding [*out* writer]
      (apply template-fn args))))

#_(defn -dump-template [fname template-fn & args]
  (apply template-fn args))

(defn new
  "Create a new ClojureCLR project from a template."
  [project name & rest]
  (cio/mkdir name)
  (write-template (cio/file name "project.clj") project-clj name)
  (write-template (cio/file name ".gitignore") gitignore)
  (cio/mkdir (cio/file name "src"))
  (cio/mkdir (cio/file name "src" name))
  (write-template (cio/file name "src" name "core.clj") core-clj name))
