(ns camp.tasks.new
  "Create new projects."
  (:require [camp.io :as cio]
            [camp.project :as cp]
            [camp.templates :as templates]))

(defn resolve-template-fn
  "Templates will be functions in the camp.project-templates namespace."
  [name]
  (let [ns (str "camp.project-template")
        ns-sym (symbol ns)
        sym (symbol ns name)]
    (when-not (find-ns ns-sym)
      (require ns-sym))
    (resolve sym)))

(defn new
  "Create a new ClojureCLR project from a template."
  [_ name & [project-template & rest]]
  (let [safe-name (.Replace name "-" "_")
        new-project (assoc cp/project-defaults :name name :safe-name safe-name)
        project-fn (resolve-template-fn (or project-template "default"))]
    (apply project-fn new-project rest)))
