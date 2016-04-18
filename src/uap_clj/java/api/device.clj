(ns uap-clj.java.api.device
  "Java API wrapper for useragent device lookup"
  (:require [uap-clj.device :refer [device-fields]]
            [clojure.walk :refer [stringify-keys]])
  (:import [java.util HashMap])
  (:gen-class
   :name uap_clj.java.api.Device
   :methods [#^{:static true} [lookup [String] java.util.HashMap]]))

(defn -lookup
  [useragent]
  (HashMap.
    (stringify-keys (device-fields useragent))))
