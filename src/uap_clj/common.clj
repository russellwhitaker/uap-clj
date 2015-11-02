(ns uap-clj.common
  (:require [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]])
  (:gen-class))

(def regexes-all (parse-string (slurp (io/resource "regexes.yaml"))))

(defn match-with-context
  "Return match result with match groups & regex replacement map,
   along with original useragent string
  "
  [line regex]
  (let [regex-case-insensitive? (get regex :regex_flag nil)
        re (if regex-case-insensitive?
             (str "(?i)" (:regex regex))
             (:regex regex))
        result (re-find (re-pattern re) line)]
    {:ua line :result result :regex (merge regex {:regex re})}))

(defn first-match
  "The uaparser/core specification indicates that for each type
   (browser, o/s, device), the first successful match for a regex
   in regexes.yaml is used, and any subsequent successful matches
   are discarded.

   If no regex successfully matches, then 'Other' is returned, which
   later is used as the 'family' replacement for each of the types.
  "
  [ua regexes]
  (let [results (map #(match-with-context ua %) regexes)]
    (or (first (remove #(nil? (:result %)) results))
        {:ua ua :result "Other"})))

(defn lookup-field
  "Extract individual type field or supply an alternate substitute
  "
  [match field index-of-alternate]
  (let [result (flatten (vector (:result match)))
        matched-substring (first result)
        regex (:regex match)
        match-pattern (re-pattern (:regex regex))]
    (trim (clojure.string/replace
            matched-substring
            match-pattern
            (get regex field
              (str (nth result index-of-alternate "")))))))

