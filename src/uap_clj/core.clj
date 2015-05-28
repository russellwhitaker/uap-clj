(ns uap-clj.core
  (:require [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]])
  (:gen-class))

(def regexes-all (parse-string (slurp (io/resource "regexes.yaml"))))

(def regexes-browser (:user_agent_parsers regexes-all))
(def regexes-os (:os_parsers regexes-all))
(def regexes-device (:device_parsers regexes-all))

;;;
;;; 'columns' and 'header' are used only for the commandline
;;;   TSV output use case.
;;;
(def columns ["useragent"
              "browser family"
                "browser major" "browser minor" "browser patch"
              "os family"
                "os major" "os minor" "os patch" "os patch minor"
              "device family"
                "device brand" "device model"])
(def header (str (s/join \tab columns) \newline))

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

(defn extract-browser-fields
  "Extract browser family, major number, minor number, and patch number
   from user agent string
  "
  [ua regexes]
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
      {:family "Other" :major nil :minor nil :patch nil})))

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

(defn lookup-useragent
  "Takes a line with a raw useragent string and does
   browser, O/S, and device lookup
  "
  [line]
  (let [browser (extract-browser-fields line regexes-browser)
        os (extract-os-fields line regexes-os)
        device (extract-device-fields line regexes-device)]
    {:ua line :browser browser :os os :device device}))

(defn -main
  "Takes a filename from the commandline containing one useragent per line,
   and writes (or overwrites) a TSV (tab-separated) file with a header.

   If no output file is provided as an argument,
   'useragent_lookup.tsv' is the name of the default output file.
  "
  [in-filename & optional-args]
  (with-open [rdr (clojure.java.io/reader in-filename)]
    (let [out-filename (or (first optional-args) "./useragent_lookup.tsv")
          results (doall
                    (map lookup-useragent (line-seq rdr)))]
      (with-open [wtr (clojure.java.io/writer out-filename :append false)]
        (.write wtr header)
        (doseq [ua results]
          (.write wtr (str (:ua ua) \tab
                           (:family (:browser ua)) \tab
                           (:major (:browser ua)) \tab
                           (:minor (:browser ua)) \tab
                           (:patch (:browser ua)) \tab
                           (:family (:os ua)) \tab
                           (:major (:os ua)) \tab
                           (:minor (:os ua)) \tab
                           (:patch (:os ua)) \tab
                           (:patch_minor (:os ua)) \tab
                           (:family (:device ua)) \tab
                           (:brand (:device ua)) \tab
                           (:model (:device ua)) \newline)))))))
