;; -*- compile-command: "msbuild /t:CampExe /verbosity:minimal && targets\camp.main.exe new scratch-project"
(ns camp.main
  (:require [camp.core :as core]
            [camp.project :as proj])
  (:gen-class))

(defn- update-project-if [project switch path val]
  (if switch (assoc-in project path val) project))

(defn- arg? [arg-set & names]
  (some arg-set names))

(defn- parse-options
  "Check for command shared flags in the args and adjust the project accordingly."
  [args]
  (let [arg-set (set args)
        debug? (arg? arg-set "--debug" "-vv")
        verbose? (or debug? (arg? arg-set "--verbose" "-v"))
        info? (or verbose? (not (arg? arg-set "--quiet" "-q")))]
    (assoc core/*options* :debug?  debug? :verbose? verbose? :info? info?)))

(defn- apply-task [project args]
  (let [task-name (or (first args) "help")
        task (core/resolve-task task-name)]
    (or (apply task project (rest args)) 0)))

(defn -main
  "Entry point for camp. Resolve first argument as camp.tasks.<name>/<name>,
load project.clj if any, then invoke task function with project as first argument
  and rest of arguments."
  [& args]
  (binding [core/*options* (parse-options args)]
    (let [project (proj/read-project)]
      (try
        (apply-task project args)
        (catch Exception e
          (println (str "Error: "
                        (if (:verbose? project)
                          (.ToString e)
                          (.Message e))))
          -1)))))
