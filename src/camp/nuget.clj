(assembly-load "NuGet.Core")

(ns camp.nuget
  "Functions for working with nuget."
  (:require [camp.io :as io])
  (:import [System.Runtime.Versioning FrameworkName]
           [NuGet
            IFrameworkTargetable
            IPackage
            LocalPackageRepository
            PackageExtensions PackageHelper
            PackageManager PackageRepositoryFactory
            PackageReference SemanticVersion
            VersionUtility]))

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

(defn target-framework
  "Get the target framework of a package."
  [package]
  (.TargetFramework package))

(defn target-framework?
  [proj ^IFrameworkTargetable ft]
  (some (partial = (framework proj)) (.SupportedFrameworks ft)))

(defn full-name
  "Get the full name of a package object."
  [^IPackage pkg]
  (str (.Id pkg) "." (.Version pkg)))

(defn package-ref
  "Convert a dependency in a project to a NuGet PackageReference."
  [proj [id ver]]
  (let [ver (semver ver)
        fw (framework proj)]
    (PackageReference. (str id) ver nil fw false false)))

(defn find-package
  "Finds a locally installed package object."
  [pm [id ver]]
  (let [repo (.LocalRepository pm)]
    (.FindPackage repo (str id) (semver ver))))

(defn pkg-files
  "Get all the files in all the packages in a project."
  [proj pm [id ver]]
  (let [repo (.LocalRepository pm)]
    (map (partial io/file (:packages-path proj))
         (.GetPackageLookupPaths repo (str id) (semver ver)))))

(defn compatible-files
  ([proj selector]
   (mapcat (partial compatible-files proj selector) (:dependencies proj)))
  ([{packages-path :packages-path :as proj} selector dep]
   (let [pm (package-mgr proj)
         pkg (find-package pm dep)]
     (->> pkg
          selector
          (filter (partial target-framework? proj))
          (map (partial io/file packages-path (full-name pkg)))))))

(defn libs
  "Get the libs from all the dependencies in the project."
  ([proj]
   (compatible-files proj #(PackageExtensions/GetLibFiles %))))

(defn tools
  "Get the tools from all the dependencies in the project."
  ([proj]
   (compatible-files proj #(PackageExtensions/GetToolFiles %))))

(defn content
  "Get the content from all the dependencies in the project."
  ([proj]
   (compatible-files proj #(PackageExtensions/GetContentFiles %))))

(defn installed?
  "Check to see if a dependency is installed locally."
  [proj pm dep]
  (not (nil? (some io/file-exists? (pkg-files proj pm dep)))))

(defn install!
  "Install a dependency locally."
  [pm [id ver]]
  (.InstallPackage pm (str id) (semver ver) false false))

