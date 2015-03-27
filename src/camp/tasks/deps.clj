(ns camp.tasks.deps
  "Functions for managing dependencies in the project."
  (:require [camp.nuget :as nuget]))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies] :as proj} & _]
  (nuget/install! proj dependencies))
