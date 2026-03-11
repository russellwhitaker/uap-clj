(ns uap-clj.conf
  "Configuration setup for uap-clj.core
  "
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io :refer [resource as-file]]))

(defn load-edn
  "Read and parse an EDN file from a path, URL, or resource.
  "
  [source]
  (with-open [r (io/reader source)]
    (edn/read (java.io.PushbackReader. r))))

(defn load-config
  "Load base config and optionally merge a local override.
  "
  []
  (let [base (load-edn (resource "resources/config.edn"))
        local-file (io/as-file ".private/config.edn")]
    (if (.exists local-file)
      (merge base (load-edn local-file))
      base)))
