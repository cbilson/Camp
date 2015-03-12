(assembly-load "Westwind.RazorHosting")

(ns camp.tasks.new
  (:require [clojure.clr.io :as io]
            [clojure.string :as str]
            [camp.core :refer [getenv to-dictionary error verbose]]
            [camp.io :as cio]
            [camp.project :as cp])
  (:import [Westwind.RazorHosting RazorEngine]))

(defn razor-engine []
  (RazorEngine.))

(defn render-template [project engine template]
  (.RenderTemplate engine template project))

(defn ->dynamic
  "Attempt to make a dynamic object for model.
  ***DOESN'T WORK***
  When the razor template tries to use properties on this dynamic object,
  I get a NotImplementedException."
  [val]
  (cond
    (map? val)
    (let [obj (System.Dynamic.ExpandoObject.)
          obj-dict (cast |System.Collections.Generic.IDictionary`2[System.String, System.Object]| obj)]
      (println "typeof obj-dict:" (class obj-dict))
      (doseq [key (keys val)
              :let [prop-name (-> key name (str/replace "-" "_"))]]
        (println "Prop Name:" (pr-str prop-name))
        (let [prop-val (->dynamic (val key))]
          (println "Prop Val:" (pr-str prop-val))
          (.Add obj-dict prop-name prop-val)))
      obj)

    (coll? val)
    (let [coll (System.Collections.ArrayList.)]
      (doseq [item val]
        (.Add coll (->dynamic item)))
      coll)

    (keyword? val)
    (name val)

    :otherwise
    val))

(defn project->model
  "Until I can get ->dynamic to work correctly, templates will use a simple
  dictionary model, which they can access like: @Model[\"name\"]."
  [project]
  (reduce (fn [m k] (assoc m (name k)(project k))) {} (keys project) ))

(defn eval-template
  "Evaluate a template in the context of a project, returning the resulting
   string."
  [project name]
  (let [asm (assembly-load "camp.resources")
        engine (razor-engine)
        resource-name (str "templates." name)
        model (project->model project)]
    (with-open [stream (.GetManifestResourceStream asm resource-name)
                reader (System.IO.StreamReader. stream)
                writer (System.IO.StringWriter.)]
      (.RenderTemplate engine reader model writer)
      (let [error-message (.ErrorMessage engine)]
        (when (not (System.String/IsNullOrWhiteSpace error-message)))
        (error error-message))
      (let [result (.ToString writer)]
        (verbose result)
        result))))

(defn write-template
  "Eval a template and write the result out to a file."
  [project template-name fname]
  (with-open [writer (io/text-writer fname)]
    (->> (eval-template project template-name)
         (.Write writer))))

(defn new
  "Create a new ClojureCLR project from a template."
  [_ name & rest]
  (let [safe-name (.Replace name "-" "_")
        new-project (assoc cp/project-defaults :name name :safe-name safe-name)]
    (cio/mkdir safe-name)
    (write-template new-project "project" (cio/file safe-name "project.clj"))
    (write-template new-project "gitignore" (cio/file safe-name ".gitignore"))
    (cio/mkdir (cio/file safe-name "src"))
    (cio/mkdir (cio/file safe-name "src" safe-name))
    (write-template new-project "core.clj"
                    (cio/file safe-name "src" safe-name "core.clj"))
    (println "Created a new ClojureCLR camp project named" name
             "in the directory" safe-name)))
