(ns camp.tasks.compile
  (:refer-clojure :exclude [compile])
  (:require [camp.io :as io] 
            [camp.tasks.deps :as deps]))

(defn compile
  "Compile the project into assemblies and exes."
  [{:keys [source-paths] :as proj} & _]
  (doseq [source-path source-paths]
    (binding [clojure.core/*compile-path* source-path]
      (println "*compile-path:" *compile-path*)
      (doseq [source-file (io/files source-path "*.clj" :AllDirectories)]
        (println "compiling" source-file)
        (compile source-file)))))
