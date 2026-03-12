(ns uap-clj.java.api.device
  "Java API wrapper for useragent device lookup"
  (:gen-class
   :name uap_clj.java.api.Device
   :methods [#^{:static true} [lookup [String] java.util.HashMap]])
  (:require
   [clojure.walk :refer [stringify-keys]]
   [uap-clj.device :refer [device]])
  (:import
   (java.util
    HashMap)))


(defn -lookup
  [useragent]
  (HashMap.
   (stringify-keys (device useragent))))
