(assembly-load "Westwind.RazorHosting")

(ns camp.tasks.new
  (:require [clojure.clr.io :as io]
            [camp.core :refer [getenv to-dictionary]]
            [camp.io :as cio])
  (:import [Westwind.RazorHosting RazorEngine]))

(defn razor-engine []
  (RazorEngine.))

(defn render-template [project engine template]
  (.RenderTemplate engine template project))

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
  (println "(ns"(str name ".core"))
  (println "  (:gen-class))")
  (println "")
  (println "(defn -main [& args]")
  (println "  (println \"TODO: something\"))"))

(defn write-template [fname template-fn & args]
  (with-open [writer (io/text-writer fname)]
    (binding [*out* writer]
      (apply template-fn args))))

#_(defn -dump-template [fname template-fn & args]
    (apply template-fn args))

(defn camp-resource [name]
  )

(defn eval-template [project name]
  (let [asm (assembly-load "camp.resources")
        engine (razor-engine)
        resource-name (str "templates." name)]
    (with-open [stream (.GetManifestResourceStream asm resource-name)
                reader (System.IO.StreamReader. stream)
                writer (System.IO.StringWriter.)]
      (.RenderTemplate engine reader project writer)
      (.ToString writer))))

(defn new
  "Create a new ClojureCLR project from a template."
  [project name & rest]
  (prn (eval-template project "gitignore"))
  (cio/mkdir name)
  (write-template (cio/file name "project.clj") project-clj name)
  (write-template (cio/file name ".gitignore") gitignore)
  (cio/mkdir (cio/file name "src"))
  (cio/mkdir (cio/file name "src" name))
  (write-template (cio/file name "src" name "core.clj") core-clj name)
  (println "Created a new ClojureCLR camp project named" name
           "in the directory" name))
