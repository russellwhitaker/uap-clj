(ns uap-clj.java.api.device
  (:require [uap-clj.device :refer [extract-device-fields]]
            [clojure.walk :refer [stringify-keys]])
  (:import [java.util HashMap])
  (:gen-class
   :name uap_clj.java.api.Device
   :methods [#^{:static true} [lookup [String] java.util.HashMap]]))

(defn -lookup
  [useragent]
  (HashMap.
    (stringify-keys (extract-device-fields useragent))))
