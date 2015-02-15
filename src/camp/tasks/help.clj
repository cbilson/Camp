(ns camp.tasks.help
  (:require [camp.core :as core]))

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
"))

(defn help
  "Print out help about how to use camp."
  [project & rest]
  (banner)
  ;; TODO: Enumerate all the tasks
  (println "TODO: implement help"))
