(ns camp.project-template
  "Built-in templates for new camp projects."
  (:require [camp.io :as cio]
            [camp.templates :as t]))

(defn webapp
  "Create a web application project that uses webapi."
  [{:keys [name safe-name] :as project} & _]
  (let [model (t/project->model project)
        asm (assembly-load "camp.resources")
        render (fn [t f]
                 (t/render-template-to-file model asm (str "templates." t) f))]
    (cio/mkdir name)
    (render "webapp.project.clj" (cio/file name "project.clj"))
    (render "gitignore" (cio/file name ".gitignore"))
    (cio/mkdir (cio/file name "src"))
    (cio/mkdir (cio/file name "src" safe-name))
    (render "webapp.core.clj" (cio/file name "src" safe-name "core.clj"))
    
    (println "Created a new ClojureCLR webapp camp project named" name ".")))

(defn default
  "Create a default project, which has a core.clj and produces an exe."
  [{:keys [name safe-name] :as project} & _]
  (let [model (t/project->model project)
        asm (assembly-load "camp.resources")
        render (fn [t f]
                 (t/render-template-to-file model asm (str "templates." t) f))]
    (cio/mkdir name)
    (render "project.clj" (cio/file name "project.clj"))
    (render "gitignore" (cio/file name ".gitignore"))
    (cio/mkdir (cio/file name "src"))
    (cio/mkdir (cio/file name "src" safe-name))
    (render "core.clj" (cio/file name "src" safe-name "core.clj"))
    
    (println "Created a new default ClojureCLR camp project named" name ".")))
