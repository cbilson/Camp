(ns camp.project
  (:require [clojure.walk :as walk]
            [camp.io :as io]))

(declare unquote-project)

(defn- unquote-step
  [arg]
  (cond
    ;; seq starting with unquote? elide the unquote and let it get
    ;; eval'd.
    (and (seq? arg) (= `unquote (first arg)))
    (second arg)

    ;; Other seq or symbol? quote it
    (or (seq? arg) (symbol? arg))
    (list 'quote arg)

    ;; not seq or symbol? recurse
    :else
    (let [result (unquote-project arg)]
      (if-let [m (meta arg)]
        ;; clojure.walk strips metadata
        (with-meta result m)
        result))))

(defn- unquote-project
  "Carefully unquotes/quotes things in the project."
  [args]
  (walk/walk unquote-step identity args))

(def project-defaults
  {:target-framework "net40"
   :nuget-repository "https://nuget.org/api/v2"
   :root (io/current-directory)
   :packages-path "packages"
   :source-paths ["src"]
   :dependencies [['Clojure "1.6.0.1"]]})

(defmacro defproject
  "Defines a project."
  [project-name version & args]
  `(let [args# ~(merge project-defaults
                       (apply hash-map (unquote-project args))
                       {:name (str project-name)
                        :version version})]
     (def ~'project args#)))

(defn eval-project [file-name]
  (binding [*ns* (find-ns 'camp.project)]
    (load-file file-name))
  (let [project (resolve 'camp.project/project)]
    (ns-unmap 'camp.project 'project)
    @project))

(defn read-project
  "Read  the project file."
  ([] (read-project "project.clj"))
  ([project-file-name]
   (if-not (io/file-exists? project-file-name)
     {}
     (eval-project project-file-name))))
