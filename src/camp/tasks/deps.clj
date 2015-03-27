(ns camp.tasks.deps
  "Functions for managing dependencies in the project."
  (:require [camp.core :refer [verbose debug]]
            [camp.nuget :as nuget]
            [camp.io :as io]))

(defn deps
  "Fetch dependencies defined in the project file."
  [{:keys [dependencies targets-path] :as proj} & _]
  (nuget/install! proj dependencies)
  
  (if (not (io/directory-exists? targets-path))
    (when (not (io/directory-exists? targets-path))
      (verbose "Creating targets folder")
      (io/mkdir targets-path)))
  
  (doseq [lib (nuget/libs proj)]
    (io/copy lib (io/file targets-path (io/file-name-only lib)) true)))
