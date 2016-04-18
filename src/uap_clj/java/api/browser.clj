(ns uap-clj.java.api.browser
  "Java API wrapper for useragent browser lookup"
  (:require [uap-clj.browser :refer [browser-fields]]
            [clojure.walk :refer [stringify-keys]])
  (:import [java.util HashMap])
  (:gen-class
   :name uap_clj.java.api.Browser
   :methods [#^{:static true} [lookup [String] java.util.HashMap]]))

(defn -lookup
  [useragent]
  (HashMap.
    (stringify-keys (browser-fields useragent))))
