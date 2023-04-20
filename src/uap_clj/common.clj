(ns uap-clj.common
  "Common functions for field matching"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]]))

(def regexes-all (edn/read-string {:reader {'ordered/map sorted-map}}
                                  (slurp (io/resource "regexes.edn"))))

(defn match-with-context
  "Return match result with match groups & regex replacement map,
   along with original useragent string
  "
  [line regex]
  (try
    (let [regex-case-insensitive? (get regex :regex_flag nil)
          re (if regex-case-insensitive?
               (str "(?i)" (:regex regex))
               (:regex regex))
          result (re-find (re-pattern re) line)]
      {:ua line :result result :regex (merge regex {:regex re})})
  (catch java.lang.NullPointerException e
    {:ua line :result {} :regex (merge regex {:regex nil})})))

(defn first-match
  "The uaparser/core specification indicates that for each type
   (browser, o/s, device), the first successful match for a regex
   in regexes.yaml shall be used.

   If no regex successfully matches, then 'Other' is returned, which
   later is used as the 'family' replacement for each of the types.
  "
  [ua regexes]
  (or
    (first
      (filter #(not (nil? (:result %)))
              (map #(match-with-context ua %) regexes)))
    {:ua ua :result "Other"}))

(defn field
  "Extract individual type field or supply an alternate substitute
  "
  [match f index-of-alternate]
  (try
    (let [result (flatten (vector (:result match)))
          matched-substring (first result)
          regex (:regex match)
          match-pattern (re-pattern (:regex regex))]
      (trim (clojure.string/replace
              matched-substring
              match-pattern
              (get regex f
                (str (nth result index-of-alternate ""))))))
  (catch java.lang.NullPointerException e nil)))
