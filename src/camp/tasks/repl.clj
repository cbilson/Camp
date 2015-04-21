(ns camp.tasks.repl
  "Start a REPL."
  (:require [clojure.main]
            [camp.core :refer [debug]]
            [camp.main :as cm]
            [camp.io :as cio]
            [camp.project :as cp]
            [camp.nuget :as nuget]))

(defn repl
  "Start a REPL. The REPL will have all the source code files
  available to `require' in, and have an assembly resolve hook to load
  any dependencies from the packages directory."
  [{:keys [source-paths] :as project} & args]
  (let [repl-args (remove cm/camp-options args)
        dir (cp/resolve-relative-path project (first source-paths))]
    (debug "Changing to src dir" dir)
    (cp/with-assembly-resolution (nuget/libs project)
      (cio/with-current-directory
        dir
        (apply clojure.main/main repl-args)))))
