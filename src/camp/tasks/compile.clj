(ns camp.tasks.compile
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as str]
            [camp.io :as io]
            [camp.tasks.deps :as deps]))

(defn source-file->target-ns
  "To go from a source file path to a namespace, we need to remove
   the src part of the path, replace '_' with '-', and replace '/' with '.'."
  [path source-file]
  (-> (str/replace source-file path "")
      (str/replace #"^\\" "")
      (str/replace io/directory-separator ".")
      (str/replace "_" "-")
      (str/replace #".clj$" "")
      symbol))

(defn compile
  "Compile the project into assemblies and exes."
  [{:keys [source-paths targets-path] :as proj} & _]
  (when (not (io/directory-exists? targets-path))
    (io/mkdir targets-path))
  (let [original-load-path (Environment/GetEnvironmentVariable
                            "CLOJURE_LOAD_PATH")
        new-load-path (str/join ";" source-paths)]
    (try
      (println "Setting CLOJURE_LOAD_PATH to" new-load-path)
      (Environment/SetEnvironmentVariable "CLOJURE_LOAD_PATH" new-load-path)
      (println "Binding *compile-path*: " targets-path)
      (println "Binding *compile-files*: " true)
      (binding [clojure.core/*compile-path* targets-path
                clojure.core/*compile-files* true]
        (doseq [source-path source-paths]
          (doseq [source-file (io/files source-path "*.clj" :AllDirectories)]
            (println "compiling" source-file)
            (let [target-ns (source-file->target-ns source-path source-file)]
              (println "ns:" target-ns)
              (clojure.core/compile target-ns)))))
      (finally
        (Environment/SetEnvironmentVariable "CLOJURE_LOAD_PATH"
                                            original-load-path)))))
