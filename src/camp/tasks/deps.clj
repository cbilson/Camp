(assembly-load-from "..\\targets\\NuGet.Core.dll")

(ns camp.tasks.deps
  (:require [camp.io :as io])
  (:import [NuGet PackageHelper
            PackageManager PackageRepositoryFactory
            PackageReference SemanticVersion
            VersionUtility]))

(defn- make-repository [{repository :repository}]
  (.CreateRepository PackageRepositoryFactory/Default repository))

(defn- make-package-manager [project]
  (let [remote-repository (make-repository project)]
    (PackageManager. remote-repository "packages")))

;; TODO: Allow use of version constraints?
(defn- dependency->PackageReference
  [{target-framework :target-framework :as project} [id version]]
  (let [semantic-version (SemanticVersion/Parse version)
        framework-name (VersionUtility/ParseFrameworkName target-framework)]
    (PackageReference.
     (str id)
     semantic-version
     nil
     framework-name
     false
     false)))

(defn- installed? [package-manager package-ref]
  (let [local-repo (.LocalRepository package-manager)
        paths (.GetPackageLookupPaths local-repo
                                      (.Id package-ref)
                                      (.Version package-ref))]
    (println "paths: " (pr-str (seq paths)))
    (not (nil? (some io/directory-exists? paths)))))

(defn- install-package! [package-manager package-ref]
  (let [source-repository (.SourceRepository package-manager)]
    (.InstallPackage package-manager
                     (.Id package-ref)
                     (.Version package-ref)
                     false
                     false)))

(defn- ensure-package-installed! [project package-manager dependency]
  (let [package-ref (dependency->PackageReference project dependency)]
    (when (not (installed? package-manager package-ref))
      (println "Installing" package-ref)
      ;; TODO: Handle consent
      (install-package! package-manager package-ref))))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as project} & _]
  (let [package-manager (make-package-manager project)]
    (doseq [dependency dependencies]
      (ensure-package-installed! project package-manager dependency))))
