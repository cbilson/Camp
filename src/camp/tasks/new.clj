(ns camp.tasks.new
  "Create new projects."
  (:require [camp.io :as cio]
            [camp.project :as cp]
            [camp.templates :as templates]))

(defn- resolve-template-fn
  "Templates will be functions in the camp.project-templates namespace."
  [name]
  (let [ns (str "camp.project-template")
        ns-sym (symbol ns)
        sym (symbol ns name)]
    (when-not (find-ns ns-sym)
      (require ns-sym))
    (resolve sym)))

(defn new
  "Create a new ClojureCLR project from a template.

  camp new <project-name>
  -----------------------
  Creates a new project with the specified name.

  camp new <template> <project-name>
  ----------------------------------
  Creates a new project using the specified template.

  Available Templates:
  
  <default>
  Creates a project.clj, README, and core.clj.
  
  webapp
  Creates a web application project, using OWIN, with all the
  necessary dependencies, a project.clj, and a core.clj that
  creates a basic self-hosted web application."
  ;; TODO: Enumerate all the available templates
  [_  & [template name & rest]]
  (let [[template name] (if (nil? name) ["default" template] [template name])
        safe-name (.Replace name "-" "_")
        new-project (assoc cp/project-defaults :name name :safe-name safe-name)
        project-fn (resolve-template-fn template)]
    (apply project-fn new-project rest)))
