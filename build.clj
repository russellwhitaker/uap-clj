(ns build
  (:require [clojure.tools.build.api :as b]))

(defn outdated
  "Check for outdated dependencies. Wraps antq.
   Usage: clojure -T:build outdated"
  [opts]
  (try
    ((requiring-resolve 'antq.tool/outdated)
     (merge {:check-clojure-tools true} opts))
    (catch clojure.lang.ExceptionInfo e
      (when-not (= "Exited" (.getMessage e))
        (throw e)))))
