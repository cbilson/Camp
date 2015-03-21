(ns camp.tasks.clean
  "Task to clean up any generated artifacts in a camp project."
  (:require [camp.core :refer [info]]
            [camp.io :as io]))

(defn clean
  "Clean up an generated artifacts in a project."
  [project & options]
  (when-let [dir (io/directory-exists? (:targets-path))]
    (info "Removing" dir)
    (io/remove-dir! dir))
  (when (some #{"--scorch"} options)
    (when-let [dir (io/directory-exists? (:packages-path project))]
      (info "Removing" dir)
      (io/remove-dir! dir))))
