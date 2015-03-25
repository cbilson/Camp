(assembly-load "NuGet.Core")

(ns camp.nuget
  "Functions for working with nuget."
  (:require [camp.core :refer [debug verbose info]]
            [camp.io :as io])
  (:import [System.Runtime.Versioning FrameworkName]
           [NuGet IPackage IPackageRepository LocalPackageRepository
            PackageExtensions PackageManager PackageRepositoryFactory
            SemanticVersion VersionUtility]))

(defn local-repo
  "Create a local package repository for a project."
  [project]
  (LocalPackageRepository. (:packages-path project)))

(defn remote-repo
  "Create a remote package repository for a project."
  [project]
  (.CreateRepository PackageRepositoryFactory/Default
                     (:nuget-repository project)))

(defn package-mgr
  "Create a package manager object for a project."
  [project]
  (PackageManager. (remote-repo project) (:packages-path project)))

(defn semver
  "Turn a string version into a nuget SemanticVersion."
  [version]
  (SemanticVersion/Parse version))

(defn framework
  "Get the proper framework version from a short nuget-ish :target-framework
  (like \"net40\") in a project."
  [{target-framework :target-framework}]
  (VersionUtility/ParseFrameworkName target-framework))

(defn full-name
  "Get the full name of a package object."
  [^IPackage pkg]
  (str (.Id pkg) "." (.Version pkg)))

(defn local-packages
  "Get all the packages instaled in a "
  [^IPackageRepository repo]
  (seq (.GetPackages repo)))

(defn pkg-files
  "Get all the files in all the packages in a project."
  [proj pm [id ver]]
  (let [repo (.LocalRepository pm)]
    (map (partial io/file (:packages-path proj))
         (.GetPackageLookupPaths repo (str id) (semver ver)))))

(defn compatible-items
  "Gets the full path to IFrameorkTargetable items in the packages
  directory that are compatible with the project's framework."
  ([proj type]
   (let [repo (local-repo proj)
         packages (local-packages repo)]
     (mapcat (partial compatible-items proj repo type) packages)))
  ([{packages-path :packages-path :as proj}
    ^LocalPackageRepository repo
    type
    ^IPackage package]
   (debug "looking for compatible" (name type) "in" (full-name package))
   (let [pm (package-mgr proj)
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

(defn installed?
  "Check to see if a dependency is installed locally."
  [proj pm dep]
  (not (nil? (some io/file-exists? (pkg-files proj pm dep)))))

(defn install!
  "Install a dependency locally."
  [pm [id ver :as dep]]
  (info "Installing" dep)
  (.InstallPackage pm (str id) (semver ver) false false))
