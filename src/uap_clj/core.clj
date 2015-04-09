(ns uap-clj.core
  (:require [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join]])
  (:gen-class))

(def regexes-all (parse-string (slurp (io/resource "regexes.yaml"))))

(def regexes-browser (:user_agent_parsers regexes-all))
(def regexes-os (:os_parsers regexes-all))

(def columns ["useragent" "browser family" "browser major"
              "browser minor" "browser patch" "os family"
              "os major" "os minor" "os patch"])
(def header (str (s/join \tab columns) \newline))

(defn match-with-context
  "Return match result with match groups & regex replacement map"
  [line regex]
  (let [result (re-find (re-pattern (:regex regex)) line)]
    {:result result :regex regex}))

(defn get-family
  "If :family_replacement exists, use it and interpolate values as necessary,
   else return 'Other'"
  [result regex replacement-category]
  (let [alternate (nth result 1 nil)]
    (clojure.string/replace
      (or
        (get regex replacement-category alternate)
        "Other")
      #"\$1" alternate)))

(defn extract-browser-fields
  "Extract browser family, major number, minor number, and patch number
   from user agent string"
  [ua regexes]
  (let [result-scan (map #(match-with-context ua %) regexes)
        first-hit (first (remove #(= nil (:result %)) result-scan))
        regex (get first-hit :regex)
        result (flatten (vector (get first-hit :result)))
        family (get-family result regex :family_replacement)
        major (nth result 2 nil)
        minor (nth result 3 nil)
        patch (nth result 4 nil)]
    {:family family :major major :minor minor :patch patch}))

(defn extract-os-fields
  "Extract o/s family, major number, minor number, and patch number
   from user agent string"
  [ua regexes]
  (let [result-scan (map #(match-with-context ua %) regexes)
        first-hit (first (remove #(= nil (:result %)) result-scan))
        regex (get first-hit :regex)
        result (flatten (vector (get first-hit :result)))
        family (get-family result regex :os_replacement)
        major (nth result 2 nil)
        minor (nth result 3 nil)
        patch (nth result 4 nil)]
    {:family family :major major :minor minor :patch patch}))

(defn lookup-line
  "Takes a line with a raw useragent string and does browser and O/S lookup"
  [line]
  (let [browser (extract-browser-fields line regexes-browser)
        os (extract-os-fields line regexes-os)]
    {:ua line :browser browser :os os}))

(defn -main
  "Takes a filename from the commandline containing one useragent per line,
   and writes (or overwrites) a TSV (tab-separated) file with a header.

   If no output file is provided as an argument, 'output.tsv' is the default.
  "
  [in-filename & optional-args]
  (with-open [rdr (clojure.java.io/reader in-filename)]
    (let [out-filename (or (first optional-args) "./output.tsv")
          results (doall
                    (map lookup-line (line-seq rdr)))]
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
                           (:patch (:os ua)) \newline)))))))
