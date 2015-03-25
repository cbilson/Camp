(ns camp.tasks.compile
  "Compiles source files into assemblies. Responsible for setting up the targets
  directory for compilation."
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as str]
            [camp.core :refer [env debug verbose info warn error]]
            [camp.io :as io]
            [camp.nuget :as nuget]
            [camp.project :as p]
            [camp.tasks.deps :as deps]))

(defn- copy-dep-libs
  "Make sure libs from all of the project's dependencies are in the
  targets directory."
  [{:keys [targets-path] :as proj}]
  (doseq [source-file-name (nuget/libs proj)]
    (debug "lib:" source-file-name)
    (let [file-name (io/file-name-only source-file-name)
          destination-file-name (io/file targets-path file-name)]
      (when (or (not (io/file-exists? destination-file-name))
                (io/newer? source-file-name destination-file-name))
        (verbose "Copying" source-file-name "to" destination-file-name)
        (io/copy source-file-name destination-file-name true)))))

(defn- source-file->target-ns
  "To go from a source file path to a namespace, we need to remove
  the src part of the path, replace '_' with '-', and replace '/' with '.'."
  [source-path source-file]
  (-> (str/replace source-file source-path "")
      (str/replace #"^\\" "")
      (str/replace io/directory-separator ".")
      (str/replace "_" "-")
      (str/replace #".clj$" "")
      symbol))

(defn- source-file->target-assembly
  "To go from a source file path to a namespace, we need to remove
  the src part of the path, replace '_' with '-', and replace '/' with '.'."
  [{:keys [targets-dir]} source-path source-file]
  (-> (str/replace source-file source-path "")
      (str/replace #"^\\" "")
      (str/replace io/directory-separator ".")
      (String/Concat ".dll")))

;;
;; We'll use a map with the following keys to track information about sources in
;; the project:
;;
;; | Key     | Meaning                                                  |
;; +---------+----------------------------------------------------------+
;; | :path   | path relative to the root of the project                 |
;; | :ns     | the namespace the source should produce                  |
;; | :target | the target assembly the source file should compile to    |
;; | :stale? | true if the target assembly is older than the source.    |
;;
(defn- analyze-sources [{:keys [source-paths] :as project}]
  (for [path source-paths]
    {:path path
     :sources
     (for [source-file (io/files path "*.clj" :AllDirectories)
           :let [target (source-file->target-assembly project path source-file)]]
       {:path source-file
        :root path
        :ns (source-file->target-ns path source-file)
        :target target
        :stale? (io/newer? source-file target)})}))

(defn- compile-source-file
  "Compile a single source file."
  [{target-ns :ns}]
  (verbose "\t\tCompiling" target-ns)
  (clojure.core/compile target-ns))

;; TODO: Come up with a better way to work with the compiler. Maybe use the
;; GenContext and Compiler classes in the clojure compiler directly, to avoid
;; generating so many assemblies, be able to add assembly attributes, and other
;; niceties.
(defn- compile-sources
  "Compile the source files in the project to assemblies, one per file."
  [{:keys [name targets-path] :as project}]
  (info "Compiling" name)
  (doseq [{src-path :path sources :sources} (analyze-sources project)
          :let [stale (->> sources (filter :stale?))]]
    (when (not (empty? stale))
      (env "CLOJURE_LOAD_PATH" (p/resolve-relative-path project src-path))
      (binding [*compile-path* (p/resolve-relative-path project targets-path)
                *compile-files* true]
        (debug "*compile-path*:" *compile-path*)
        (debug "*compile-files*:" *compile-files*)

        (doseq [ns (map :ns stale)]
          (try
            (verbose "\tcompiling" ns)
            (clojure.core/compile ns)
            (catch Exception ex
              (error "Failed compiling" ns)
              (error (.ToString ex))
              (throw ex))))))))

(defn compile
  "Compile task, to compile the project into assemblies and exes."
  [{:keys [targets-path] :as proj} & _]
  (try
    (verbose "Checking deps" targets-path)
    (deps/deps proj)
    (when (not (io/directory-exists? targets-path))
      (verbose "Creating targets folder")
      (io/mkdir targets-path))
    (copy-dep-libs proj)

    (p/with-assembly-resolution
      proj
      (compile-sources proj))

    (catch Exception ex
      (error "Error:" (.Message ex))
      (verbose (.ToString ex)))))
