(ns camp.tasks.compile
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as str]
            [camp.core :refer [getenv setenv verbose]]
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

(defn- compile-source-file
  "Compile a single source file."
  [source-dir source-file]
  (let [target-ns (source-file->target-ns source-dir source-file)]
    (verbose "\t\tCompiling" target-ns)
    (clojure.core/compile target-ns)))

(defn- compile-source-directory
  "Compile one source directory in the project."
  [source-dir]
  (doseq [source-file (io/files source-dir "*.clj" :AllDirectories)]
    (verbose "\tCompiling" source-file)
    (compile-source-file source-dir source-file)))

(defn- compile-all-source-directories
  "Compile each source directory in the project."
  [{source-paths :source-paths}]
  (doseq [source-dir source-paths]
    (verbose "Compiling" source-dir)
    (compile-source-directory source-dir)))

(defn- with-compile-env
  "Setup environment variables and binding to compile the project,
  invoke some function in that environment, and cleanup afterwords."
  [{:keys [targets-path source-paths] :as project} f]
  (let [original-load-path (getenv "CLOJURE_LOAD_PATH")
        new-load-path (str/join ";" source-paths)]
    (try
      (setenv "CLOJURE_LOAD_PATH" new-load-path)
      (binding [clojure.core/*compile-path* targets-path
                clojure.core/*compile-files* true]
        (f project))
      (finally
        (setenv "CLOJURE_LOAD_PATH" original-load-path)))))

(defn- copy-dep-libs
  "Make sure libs from all of the project's dependencies are in the
  targets directory."
  [{:keys [targets-path] :as proj}]
  (doseq [source-file-name (deps/libs proj)]
    (let [file-name (io/file-name-only source-file-name)
          destination-file-name (io/file targets-path file-name)]
      (when (not (io/file-exists? destination-file-name))
        (verbose "Copying" source-file-name "to" destination-file-name)
        (io/copy source-file-name destination-file-name)))))

(defn compile
  "Compile the project into assemblies and exes."
  [{:keys [targets-path] :as proj} & _]
  (verbose "Checking deps" targets-path)
  (deps/deps proj)
  (when (not (io/directory-exists? targets-path))
    (verbose "Creating targets folder")
    (io/mkdir targets-path))
  (copy-dep-libs proj)
  (with-compile-env proj compile-all-source-directories))
