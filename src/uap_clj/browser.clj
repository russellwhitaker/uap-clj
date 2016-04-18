(ns uap-clj.browser
  "Useragent browser lookup"
  (:require [uap-clj.common :refer [regexes-all first-match lookup-field]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]]))

(def regexes (:user_agent_parsers regexes-all))

(def browser-fields
  (memoize
    (fn
      [ua]
      (try
        (let [match (first-match ua regexes)
              result (first (flatten (vector (:result match))))]
          (if (= "Other" result)
            {:family "Other" :major nil :minor nil :patch nil}
            (let [family (lookup-field match :family_replacement 1)
                  major (lookup-field match :v1_replacement 2)
                  minor (lookup-field match :v2_replacement 3)
                  patch (lookup-field match :v3_replacement 4)]
              {:family family :major major :minor minor :patch patch})))
      (catch java.lang.IndexOutOfBoundsException e
        {:family "Other" :major nil :minor nil :patch nil})))))
