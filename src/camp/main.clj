;; -*- compile-command: "msbuild /t:CampExe /verbosity:minimal && targets\camp.main.exe new scratch-project"
(ns camp.main
  (:require [camp.core :as core])
  (:gen-class))

(defn -main
  "Entry point for camp. Resolve first argument as camp.tasks.<name>/<name>,
load project.clj if any, then invoke task function with project as first argument
and rest of arguments."
  [& args]
  (try
    (let [task (if-let [task-name (first args)]
                 (core/resolve-task task-name)
                 (core/resolve-task "help"))
          project (core/read-project)]
      (or (apply task project (rest args)) 0))
    (catch Exception e
      (println (str "Error: " (.ToString e)))
      -1)))
    
