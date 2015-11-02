(ns uap-clj.device
  (:require [uap-clj.common :refer [regexes-all first-match lookup-field]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]])
  (:gen-class))

(def regexes-device (:device_parsers regexes-all))

(defn extract-device-fields
  "Extract device family, brand, and model number
   from user agent string
  "
  [ua regexes]
  (try
  (let [match (first-match ua regexes)
        result (first (flatten (vector (:result match))))]
    (if (= "Other" result)
      {:family "Other" :brand nil :model nil}
      (let [family (lookup-field match :device_replacement 1)
            brand (lookup-field match :brand_replacement 2)
            model (lookup-field match :model_replacement 1)]
        {:family family :brand brand :model model})))
    (catch java.lang.IndexOutOfBoundsException e
      {:family "Other" :brand nil :model nil})))
