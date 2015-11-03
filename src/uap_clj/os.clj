(ns uap-clj.os
  (:require [uap-clj.common :refer [regexes-all first-match lookup-field]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]])
  (:gen-class))

(def regexes-os (:os_parsers regexes-all))

(defn extract-os-fields
  "Extract os/ family, major number, minor number, patch, and patch-minor number
   from user agent string
  "
  [ua regexes]
  (try
  (let [match (first-match ua regexes)
        result (first (flatten (vector (:result match))))]
    (if (= "Other" result)
      {:family "Other" :major nil :minor nil :patch nil :patch_minor nil}
      (let [family (lookup-field match :os_replacement 1)
            major (lookup-field match :os_v1_replacement 2)
            minor (lookup-field match :os_v2_replacement 3)
            patch (lookup-field match :os_v3_replacement 4)
            patch-minor (lookup-field match :os_v4_replacement 5)]
        {:family family :major major :minor minor :patch patch :patch_minor patch-minor})))
    (catch java.lang.IndexOutOfBoundsException e
      {:family "Other" :major nil :minor nil :patch nil :patch_minor nil})))
