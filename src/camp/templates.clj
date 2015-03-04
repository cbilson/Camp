(assembly-load "Westwind.RazorHosting")

(ns camp.templates
  "Functions for working with templates."
  (:require [clojure.clr.io :as io]
            [camp.core :refer [info error]])
  (:import [System.IO StreamReader]
           [Westwind.RazorHosting RazorEngine]))

(defn project->model
  "Templates that operate on projects will use a simple dictionary model,
  which they can access like: @Model[\"name\"]."
  [project]
  (reduce (fn [m k] (assoc m (name k)(project k))) {} (keys project) ))

(defn resource [assembly name]
  (let [resource (.GetManifestResourceStream assembly name)]
    (when (nil? resource)
      (throw (ex-info "Resource not found." {:assembly assembly :name name})))
    resource))

(defn resource-reader
  "Load a template from a named resource."
  [assembly name]
  (-> (resource assembly name)
      (StreamReader.)))

(defn render-template
  [model resource-assembly resource-name output-writer]
  (with-open [template-reader (resource-reader resource-assembly resource-name)]
    (let [engine (RazorEngine.)]
      (.RenderTemplate engine template-reader model output-writer)
      (let [error-message (.ErrorMessage engine)]
        (when (not (System.String/IsNullOrWhiteSpace error-message))
          (error error-message))))))

(defn render-template-to-file
  "Eval a template and write the result out to a file."
  [model resource-assembly resource-name file-name]
  (with-open [writer (io/text-writer file-name)]
    (render-template model resource-assembly resource-name writer)))
