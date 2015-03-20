(ns camp.tasks.deps
  "Functions for managing dependencies in the project."
  (:require [camp.core :refer [verbose]]
            [camp.nuget :as nuget]))

(defn- ensure-installed! [proj pm dep]
  (when (not (nuget/installed? proj pm dep))
    (verbose "Installing" dep)
    (nuget/install! pm dep)))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as proj} & _]
  (let [pm (nuget/package-mgr proj)]
    (doseq [dep dependencies]
      (ensure-installed! proj pm dep))))
