(ns camp.main
  "The camp command line program entry point."
  (:require [camp.core :as core]
            [camp.project :as proj])
  (:gen-class))

(defn- arg?
  "Determine if an argument was supplied."
  [arg-set & names]
  (not (nil? (some arg-set names))))

(def camp-options
  #{"--debug" "-vv" "--verbose" "-v" "--quiet" "-q"})

(defn- parse-options
  "Check for command shared flags in the args and adjust the project accordingly."
  [args]
  (let [arg-set (set args)
        debug? (arg? arg-set "--debug" "-vv")
        verbose? (or debug? (arg? arg-set "--verbose" "-v"))
        info? (or verbose? (not (arg? arg-set "--quiet" "-q")))]
    (assoc core/*options* :debug?  debug? :verbose? verbose? :info? info?)))

(defn- apply-task
  "Given a project and args, lookup the task (always the 1st argument)
  and apply it to the project and the rest of the arguments."
  [project args]
  (let [task-name (or (first args) "help")
        task (core/resolve-task task-name)]
    (or (apply task project (rest args)) 0)))

(defn -main
  "Entry point for camp. Resolve first argument as camp.tasks.<name>/<name>,
  load project.clj if any, then invoke task function with project as
  first argument and rest of arguments."
  [& args]
  (binding [core/*options* (parse-options args)]
    (let [project (proj/read-project)]
      (try
        (apply-task project args)
        (catch Exception e
          (core/verbose "Exception:" (.ToString e))
          (core/error "Error:" (.Message e))
          (Environment/Exit -1))))))
