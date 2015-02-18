(assembly-load-from "..\\targets\\NuGet.Core.dll")

(ns camp.tasks.deps
  (:require [camp.io :as io])
  (:import [NuGet LocalPackageRepository PackageHelper
            PackageManager PackageRepositoryFactory
            PackageReference SemanticVersion
            VersionUtility]))

(defn local-repo [project]
  (LocalPackageRepository. (:packages-dir project)))

(defn remote-repo [project]
  (.CreateRepository PackageRepositoryFactory/Default
                     (:nuget-repository project)))

(defn package-mgr [project]
  (PackageManager. (remote-repo project) (:packages-dir project)))

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
    (map (partial io/file (:packages-dir proj))
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

(defn- find-package [repo [id ver]]
  (.FindPackage repo id (semver ver)))

#_(defn libs
  "Gets all the library files in all dependency packages."
  [{:keys [packages-dir target-framework dependencies] :as proj}]
  (let [repo (local-repo proj)]
    (->> dependencies
         (map (partial find-package repo))
         (remove nil?))))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as proj} & _]
  (let [pm (package-mgr proj)]
    (doseq [dep dependencies]
      (ensure-installed! proj pm dep)))
  #_(println "Libs:" (pr-str (libs proj))))
