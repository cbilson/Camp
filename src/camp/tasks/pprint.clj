(ns camp.tasks.pprint
  "Pretty print a project."
  (:require [clojure.pprint :as pp]))

(defn pprint
  "Pretty prints the project file. Currently, this should be the same as the
  `project.clj' file you see in a text editor, with any inline code eval'd,
  but in the future when profiles and plugins are supported, this will be useful
  to help see what data is being added or removed from the project."
  [project & _]
  (pp/pprint project))
