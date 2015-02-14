(ns camp.main
  (:require [camp.core :as core])
  (:gen-class))

(defn -main [& args]
  (let [{task :task :as options} (core/parse-args args)]
    (task options)))
