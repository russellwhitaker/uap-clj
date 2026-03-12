(ns uap-clj.java.api.os
  "Java API wrapper for useragent o/s lookup"
  (:gen-class
   :name uap_clj.java.api.OS
   :methods [#^{:static true} [lookup [String] java.util.HashMap]])
  (:require
   [clojure.walk :refer [stringify-keys]]
   [uap-clj.os :refer [os]])
  (:import
   (java.util
    HashMap)))


(defn -lookup
  [useragent]
  (HashMap.
   (stringify-keys (os useragent))))
