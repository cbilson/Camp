(ns camp.tasks.clean
  "Task to clean up any generated artifacts in a camp project."
  (:require [camp.core :refer [debug info]]
            [camp.io :as io]
            [camp.project :as proj]))

(defn- remove-if-exists [proj maybe-relative-path]
  (let [path (proj/resolve-relative-path proj maybe-relative-path)]
    (debug "full path:" path)
    (cond
      (io/directory-exists? path)
      (do
        (info "Removing" path)
        (io/remove-dir! path true))

      (io/file-exists? path)
      (do
        (info "Removing" path)
        (io/remove-file! path)))))

(defn clean
  "Clean up an generated artifacts in a project."
  [project & options]
  (doseq [dir [(:targets-path project)]]
    (remove-if-exists project dir))
  (when (some #{"--scorch"} options)
    (doseq [dir [(:packages-path project)]]
      (remove-if-exists project dir))))
