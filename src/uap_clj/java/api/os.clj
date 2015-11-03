(ns uap-clj.java.api.os
  (:require [uap-clj.os :refer [regexes-os extract-os-fields]]
            [clojure.walk :refer [stringify-keys]])
  (:import [java.util HashMap])
  (:gen-class
   :name uap_clj.java.api.OS
   :methods [#^{:static true} [lookup [String] java.util.HashMap]]))

(defn -lookup
  [useragent]
  (HashMap.
    (stringify-keys (extract-os-fields useragent regexes-os))))
