(ns camp.core
  (:import [System Environment]))

(defn resolve-task
  "Given the name of a task, find the function that implements it."
  [task-name]
  (let [ns (str "camp.tasks." task-name)
        ns-sym (symbol ns)
        sym (symbol ns task-name)]
    (when-not (find-ns ns-sym)
      (require ns-sym))

    (resolve sym)))

(defn parse-args [[task & rest-args]]
  (if (not task)
    (do
      {:task (resolve-task "help")})
    (merge
     {:task (resolve-task task)}
     (apply hash-map (map read-string rest-args)))))

(defn getenv
  "Get the value of an environment variable"
  ([name] (getenv name nil))
  ([name default-value]
   (or (Environment/GetEnvironmentVariable name)
       default-value)))
