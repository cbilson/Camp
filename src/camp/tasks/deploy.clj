(ns camp.tasks.deploy
  "Functions for deploying project artifacts."
  (:require [clojure.string :as str]))

(defn clojure-name->nuget-name [name]
  (-> (str/replace name "/" ".")
      (str/replace name "/" ".")))

(defn deploy
  "Deploy a nupkg of the project to a nuget repository."
  [{:keys [name version deploy-repository]} & args]
  (let [package-name (clojure-name->nuget-name name)]
    (println "TODO: make sure there is a nupkg in targets and nuget push that.")))
