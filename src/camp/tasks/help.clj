(ns camp.tasks.help
  (:require [camp.core :as core])
  (:import [System.IO StringReader]))

(defn- banner []
  (println "
------------------------------------------------------------
   .(         .--.      .---.   ___ .-. .-.      .-..
   /%/\\       /    \\    / .-, \\ (   )   '   \\    /    \\
  (%(%))     |  .-. ;  (__) ; |  |  .-.  .-. ;  ' .-,  ;
 .-'..`-.    |  |(___)   .'`  |  | |  | |  | |  | |  . |
 `-'.'`-'dd  |  |       / .'| |  | |  | |  | |  | |  | |
             |  | ___  | /  | |  | |  | |  | |  | |  | |
             |  '(   ) ; |  ; |  | |  | |  | |  | |  ' |
             '  `-' |  ' `-'  |  | |  | |  | |  | `-'  '
              `.__,'   `.__.'_. (___)(___)(___) | \\__.'
----------------------------------------------- | | --------
A tool to get you started with ClojureCLR.     (___)

Tasks
=====
clean     Remove generated artifacts.
compile   Compile source files into assemblies.
deploy    Deploy project to a nuget server.
deps      Fetch dependencies from nuget.
help      Show this message, help <task> for more details.
new       Create a new project.
package   Create a nuget package with project artifacts.
pprint    Pretty-print the project file.
repl      Starts a REPL with the current project.
---

Use `camp help <task>' to get more details about a specific task.

"))

(defn- task-help [task-name]
  (when-let [task-fn (core/resolve-task task-name)]
    (println)
    (println "camp" task-name)
    (println (String. \- 60))

    (with-open [reader (StringReader. (:doc (meta task-fn)))]
      (loop []
        (when-let [line (.ReadLine reader)]
          (println (.Trim line))
          (recur))))
    
    (println)))

(defn help
  "Print out help about how to use camp."
  [project & [task & rest]]
  (if task
    (task-help task)
    (banner)))
