(assembly-load "NuGet.Core")

(ns camp.nuget
  "Functions for working with nuget."
  (:require [camp.core :refer [debug verbose info] :as cc]
            [camp.io :as io])
  (:import [System.Runtime.Versioning FrameworkName]
           [NuGet ILogger IPackageRepository IPackage
            DataServicePackageRepository LocalPackageRepository
            AggregateRepository MachineCache PriorityPackageRepository
            PackageRepositoryExtensions
            SharedPackageRepository PackageExtensions PackageManager
            RedirectedHttpClient PhysicalFileSystem DefaultPackagePathResolver
            SemanticVersion VersionUtility]))

(defn- nuget-logger []
  (reify ILogger
    (Log [level message & args]
      (case (keyword (Enum/GetName level))
        :Debug (apply cc/debug message args)
        :Info (apply cc/info message args)
        :Warning (apply cc/warn message args)
        :Error (apply cc/error message args)))))

(def nuget-urls
  {:default "https://www.nuget.org/api/v2/"
   :official "https://www.nuget.org/api/v2/"
   :v3 "https://az320820.vo.msecnd.net/ver3-preview/index.json"
   :v2-legacy-official "https://nuget.org/api/v2/"
   :v2-legacy "https://go.microsoft.com/fwlink/?LinkID=230477"
   :v1 "https://go.microsoft.com/fwlink/?LinkID=206669"
   :vs-express-for-windows-8
   "https://www.nuget.org/api/v2/curated-feeds/windows8-packages/"})

(defn- semver
  "Turn a string version into a nuget SemanticVersion."
  [version]
  (SemanticVersion/Parse version))

(defn- make-repository [source]
  (debug "Making repository for" source)
  (let [uri (Uri. (if (keyword? source)
                    (nuget-urls source)
                    source))]
    (if (.IsFile uri)
      (do
        (debug "Using local repository" (.LocalPath uri))
        (LocalPackageRepository. (.LocalPath uri)))
      (do
        (debug "Using remote repository" uri)
        (DataServicePackageRepository. (RedirectedHttpClient. uri))))))

(defn- aggregate-repository [{{sources :sources} :nuget :as proj}]
  (let [repos (map make-repository sources)]
    (AggregateRepository. (into-array IPackageRepository repos))))

(defn- make-cache-repository
  [proj]
  (let [agg-repo (aggregate-repository proj)]
    (if (get-in proj [:nuget :cache?])
      (do
        (debug "making cache repository")
        (PriorityPackageRepository. MachineCache/Default agg-repo))
      (do
        (debug "using aggregate repository, no caching.")
        agg-repo))))

(defn- make-package-manager
  [{packages-path :packages-path {save-mode :save-mode} :nuget :as proj}]
  (let [source-repository (make-cache-repository proj)
        fs (PhysicalFileSystem. packages-path)
        resolver (DefaultPackagePathResolver. fs)
        shared (SharedPackageRepository. resolver fs fs)]
    (when (not= save-mode :None)
      (set! (.PackageSaveMode shared) (enum-val save-mode)))
    (PackageManager. source-repository resolver fs shared)))

(defn- ^IPackage find-package
  ([repository id]
   (PackageRepositoryExtensions/FindPackage repository (str id)))
  ([repository id ver]
   (PackageRepositoryExtensions/FindPackage repository (str id) (semver ver))))

(defn- version>= [v1 v2]
  (> 0 (.CompareTo v1 v2)))

(defn- install-needed?
  [{:keys [nuget] :as proj} [id ver :as dep] pm]
  (if-let [package (find-package (.LocalRepository pm) id)]
    (version>= (.Version package) (semver ver))
    true))

(defn- start-operation! [pm operation [id ver]]
  (.. pm SourceRepository
      (StartOperation operation (str id) ver)))

(defn install!
  "Install a package. Based on command line NuGet's install command."
  [{{pre-release? :allow-pre-release?} :nuget :as proj} deps]
  ;; Create package manager
  (let [pm (make-package-manager proj)]
    (doseq [[id ver :as dep] deps]
      ;; when not installed
      (if (install-needed? proj dep pm)
        (with-open [action (start-operation! pm "Install" dep)]
          (info "Installing" dep)
          (.InstallPackage pm (str id) (semver ver) false pre-release?))
        (verbose dep "already installed")))))

(defn framework
  "Get the proper framework version from a short nuget-ish :target-framework
  (like \"net40\") in a project."
  [{target-framework :target-framework}]
  (VersionUtility/ParseFrameworkName target-framework))

(defn full-name
  "Get the full name of a package object."
  [^IPackage pkg]
  (str (.Id pkg) "." (.Version pkg)))

(defn compatible-items
  "Gets the full path to IFrameorkTargetable items in the packages
  directory that are compatible with the project's framework."
  ([proj type]
   (let [pm (make-package-manager proj)
         repo (.LocalRepository pm)
         packages (.GetPackages repo)]
     (mapcat (partial compatible-items proj repo type) packages)))
  ([{packages-path :packages-path :as proj}
    ^LocalPackageRepository repo
    type
    ^IPackage package]
   (debug "looking for compatible" (name type) "in" (full-name package))
   (let [pm (make-package-manager proj)
         selector (type {:libs #(PackageExtensions/GetLibFiles %)
                         :tools #(PackageExtensions/GetToolFiles %)
                         :content #(PackageExtensions/GetContentFiles %)})
         items (selector package)
         compatible-items nil]
     (if (VersionUtility/TryGetCompatibleItems
          (type-args NuGet.IPackageFile)
          (framework proj)
          (selector package)
          (by-ref compatible-items))
       (map #(.SourcePath %) compatible-items)
       (verbose "No compatible" (name type) "in" (full-name package))))))

(defn libs
  "Get the libs from all the dependencies in the project."
  ([proj]
   (compatible-items proj :libs)))

(defn tools
  "Get the tools from all the dependencies in the project."
  ([proj]
   (compatible-items proj :tools)))

(defn content
  "Get the content from all the dependencies in the project."
  ([proj]
   (compatible-items proj :content)))
