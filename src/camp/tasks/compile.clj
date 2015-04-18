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

(defn- create-config-file-if-newer [source target]
  (if (and (io/file-exists? target)
           (not (io/newer? source target)))
    (debug target "not older than" source ". Not updating.")
    (do
      (verbose "Creating" target "from" source)
      (io/copy source target))))

(defn- create-exe-configs [targets-path]
  (when (io/file-exists? "app.config")
    (doseq [exe (io/files targets-path "*.exe")
            :let [target (io/file targets-path (str exe ".config"))]]
      (create-config-file-if-newer "app.config" target))))

(defn- create-web-config [targets-path]
  (when (io/file-exists? "app.config")
    (io/file targets-path "web.config")))

(defn compile
  "Compiles the project into assemblies and exes.

  Any classes in the project for which the `:main' option is true (which
  is the default) will be compiled into an executable assemblies. All
  other clojure source files and classes will be compiled each into a
  separate library assembly. All compilation targets will be in the
  `targets' directory under the root of the project.

  If any dependency is missing, it is fetched before compilation starts.

  If there is an `app.config' file in the root directory of the project,
  it is copied into `targets' directory once for each executable
  target, renamed to `<target>.exe.config.'

  If there is a `web.config' file in the root directory of the project,
  it will be copied into `targets' as `web.config'."
  [{:keys [targets-path] :as proj} & _]
  (try
    (verbose "Checking deps" targets-path)
    (deps/deps proj)

    (p/with-assembly-resolution
      (nuget/libs proj)
      (compile-sources proj))

    (create-exe-configs targets-path)

    (create-web-config targets-path)

    (catch Exception ex
      (error "Error:" (.Message ex))
      (verbose (.ToString ex)))))
