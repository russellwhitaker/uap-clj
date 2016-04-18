(ns uap-clj.java.api.os
  "Java API wrapper for useragent o/s lookup"
  (:require [uap-clj.os :refer [os]]
            [clojure.walk :refer [stringify-keys]])
  (:import [java.util HashMap])
  (:gen-class
   :name uap_clj.java.api.OS
   :methods [#^{:static true} [lookup [String] java.util.HashMap]]))

(defn -lookup
  [useragent]
  (HashMap.
    (stringify-keys (os useragent))))
