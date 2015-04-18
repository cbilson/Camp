(ns camp.tasks.package
  "Package up project artifacts into a nupkg."
  (:require [camp.nuget :as nuget]))

(defn package
  "Packages up project targets into a nuget package. The equivalent of
  `nuget pack'.

  ***NOT IMPLEMENTED YET***"
  [project & _]
  (println "TODO: Gather dlls, exes, and other content in targets/")
  (println "TODO: Generate manifest")
  (println "TODO: Create a new OptimizedZipPackage"))
