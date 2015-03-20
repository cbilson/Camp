(ns camp.tasks.package
  "Package up project artifacts into a nupkg."
  (:require [camp.nuget :as nuget]))

(defn package
  "The equivalent of `nuget pack'."
  [project & _]
  (println "TODO: Gather dlls, exes, and other content in targets/")
  (println "TODO: Generate manifest")
  (println "TODO: Create a new OptimizedZipPackage"))
