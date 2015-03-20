(assembly-load "Westwind.RazorHosting")

(ns camp.templates
  "Functions for working with templates."
  (:require [clojure.clr.io :as io]
            [camp.core :refer [info error] :as core])
  (:import [System.IO StreamReader]
           [Westwind.RazorHosting RazorEngine]))

;; TODO: I'd like to use a model based on a dynamic object, but I wasn't able
;; to get that to work with clojure. I'd like the templates to have code like
;; @Model.name, instead of @Model["name"].
(defn project->model
  "Templates that operate on projects will use a simple dictionary model,
  which they can access like: @Model[\"name\"]."
  [project]
  (reduce (fn [m k] (assoc m (name k)(project k))) {} (keys project) ))

(defn render-template
  "Render a template, parameterized with a model, to a text writer."
  [model asm name out]
  (with-open [rdr (core/resource-reader asm name)]
    (let [engine (RazorEngine.)]
      (.RenderTemplate engine rdr model out)
      (let [error-message (.ErrorMessage engine)]
        (when (not (System.String/IsNullOrWhiteSpace error-message))
          (error error-message))))))

(defn render-template-to-file
  "Render a template, parameterized with a model, to a file."
  [model asm name file-name]
  (with-open [writer (io/text-writer file-name)]
    (render-template model asm name writer)))
