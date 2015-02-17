(assembly-load-from "..\\targets\\NuGet.Core.dll")

(ns camp.tasks.deps
  (:import [NuGet PackageManager SemanticVersion PackageRepositoryFactory]))

(defn- make-repository []
  (.CreateRepository PackageRepositoryFactory/Default "https://nuget.org/api/v2"))

(defn- make-package-manager []
  (let [repo (make-repository)]
    (PackageManager. (make-repository) "packages")))

(defn- install-package [package-manager [id version]]
  (let [id (str id)
        version (SemanticVersion. version)]
    (println "Installing" id version)
    (.InstallPackage package-manager id version)))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as project} & _]
  (let [package-manager (make-package-manager)]
    (doseq [dependency dependencies]
      (install-package package-manager dependency))))
