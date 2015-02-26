(let [nuget-assembly
      (System.IO.Path/Combine
       (.BaseDirectory System.AppDomain/CurrentDomain)
       "NuGet.Core.dll")]
  (if (System.IO.File/Exists nuget-assembly)
    (assembly-load-from nuget-assembly)
    ;; at compile time, load from targets directory
    (assembly-load-from "..\\targets\\NuGet.Core.dll")))

(ns camp.tasks.deps
  "Functions for managing dependencies in the project."
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

(defn local-repo [project]
  (LocalPackageRepository. (:packages-path project)))

(defn remote-repo [project]
  (.CreateRepository PackageRepositoryFactory/Default
                     (:nuget-repository project)))

(defn package-mgr [project]
  (PackageManager. (remote-repo project) (:packages-path project)))

(defn- semver [version]
  (SemanticVersion/Parse version))

(defn framework [{target-framework :target-framework}]
  (VersionUtility/ParseFrameworkName target-framework))

(defn- package-ref
  "Convert a dependency in a project to a NuGet PackageReference."
  [proj [id ver]]
  (let [ver (semver ver)
        fw (framework proj)]
    (PackageReference. (str id) ver nil fw false false)))

(defn- pkg-files [proj pm [id ver]]
  (let [repo (.LocalRepository pm)]
    (map (partial io/file (:packages-path proj))
         (.GetPackageLookupPaths repo (str id) (semver ver)))))

(defn- installed?
  "Check to see if a dependency is installed locally."
  [proj pm dep]
  (not (nil? (some io/file-exists? (pkg-files proj pm dep)))))

(defn- install!
  "Install a dependency locally."
  [pm [id ver]]
  (.InstallPackage pm (str id) (semver ver) false false))

(defn- ensure-installed! [proj pm dep]
  (when (not (installed? proj pm dep))
    (println "Installing" dep)
    (install! pm dep)))

(defn- find-package [pm [id ver]]
  (let [repo (.LocalRepository pm)]
    (.FindPackage repo (str id) (semver ver))))

(defn- target-framework [package]
  (.TargetFramework package))

(defn- target-framework? [proj ^IFrameworkTargetable ft]
  (some (partial = (framework proj)) (.SupportedFrameworks ft)))

(defn- full-name [^IPackage pkg]
  (str (.Id pkg) "." (.Version pkg)))

(defn- compatible-files
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

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as proj} & _]
  (let [pm (package-mgr proj)]
    (doseq [dep dependencies]
      (ensure-installed! proj pm dep))))
