(ns uap-clj.java.api.browser
  "Java API wrapper for useragent browser lookup"
  (:gen-class
   :name uap_clj.java.api.Browser
   :methods [#^{:static true} [lookup [String] java.util.HashMap]])
  (:require
   [clojure.walk :refer [stringify-keys]]
   [uap-clj.browser :refer [browser]])
  (:import
   (java.util
    HashMap)))


(defn -lookup
  [useragent]
  (HashMap.
   (stringify-keys (browser useragent))))
