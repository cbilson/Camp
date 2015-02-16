;; -*- compilation-command: "C:\Windows\Microsoft.NET\Framework\v4.0.30319\msbuild.exe /t:CampExe /verbosity:minimal"
(assembly-load-with-partial-name "NuGet.Core")

(ns camp.tasks.deps
  (:import [NuGet PackageManager SemanticVersion PackageRepositoryFactory]))

(defn- make-package-manager []
  (PackageManager. PackageRepositoryFactory/Default
                   (make-array String)
                   true))

(defn- install-package [package-manager [id version]]
  (let [id (str id)
        version (SemanticVersion. version)]
    (.AddPackageReference package-manager id version)))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as project} & _]
  (let [package-manager (make-package-manager)]
    (doseq [dependency dependencies]
      (install-package package-manager dependency))))
