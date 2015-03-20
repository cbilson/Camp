(ns camp.tasks.repl
  "Start a REPL."
  (:require [clojure.main]
            [camp.core :refer [debug]]
            [camp.main :as cm]
            [camp.io :as cio]
            [camp.project :as cp]))

(defn repl
  "Start a REPL."
  [{:keys [source-paths] :as project} & args]
  (let [repl-args (remove cm/camp-options args)
        dir (cp/resolve-relative-path project (first source-paths))]
    (debug "Changing to src dir" dir)
    (cp/with-assembly-resolution project
      (cio/with-current-directory
        dir
        (apply clojure.main/main repl-args)))))
