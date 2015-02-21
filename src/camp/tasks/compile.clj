(ns camp.tasks.compile
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as str]
            [camp.io :as io]
            [camp.tasks.deps :as deps]))

(defn- source-file->target-ns
  "To go from a source file path to a namespace, we need to remove
   the src part of the path, replace '_' with '-', and replace '/' with '.'."
  [source-dir source-file]
  (-> (str/replace source-file source-dir "")
      (str/replace #"^\\" "")
      (str/replace io/directory-separator ".")
      (str/replace "_" "-")
      (str/replace #".clj$" "")
      symbol))

(defn- compile-source-file [source-dir source-file]
  (let [target-ns (source-file->target-ns source-dir source-file)]
    (println "compiling" target-ns)
    (clojure.core/compile target-ns)))

(defn- compile-source-directory [source-dir]
  (doseq [source-file (io/files source-dir "*.clj" :AllDirectories)]
    (compile-source-file source-dir source-file)))

(defn compile
  "Compile the project into assemblies and exes."
  [{:keys [source-paths targets-path] :as proj} & _]
  (when (not (io/directory-exists? targets-path))
    (io/mkdir targets-path))
  (let [original-load-path (Environment/GetEnvironmentVariable
                            "CLOJURE_LOAD_PATH")
        new-load-path (str/join ";" source-paths)]
    (try
      (Environment/SetEnvironmentVariable "CLOJURE_LOAD_PATH" new-load-path)
      (binding [clojure.core/*compile-path* targets-path
                clojure.core/*compile-files* true]
        (doseq [source-dir source-paths]
          (compile-source-directory source-dir)))
      (finally
        (Environment/SetEnvironmentVariable "CLOJURE_LOAD_PATH"
                                            original-load-path)))))
