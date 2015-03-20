(ns camp.tasks.pprint
  "Pretty print a project."
  (:require [clojure.pprint :as pp]))

(defn pprint [project & _]
  (pp/pprint project))
