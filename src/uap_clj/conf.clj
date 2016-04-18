(ns uap-clj.conf
  "Configuration setup for uap-clj.core
  "
  (:require [clojure.java.io :as io :refer [resource as-file]]))

(defn base-config
  "Configuration without password secret, safe to check into SCM.
  "
  []
  (resource "resources/config.edn"))

(defn local-config
  "Optional local override.
  "
  []
  (when (.exists (as-file ".private/config.edn")) ".private/config.edn"))
