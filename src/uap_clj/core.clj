(ns uap-clj.core
  (:require [uap-clj.common :as common :refer :all]
            [uap-clj.browser :refer [extract-browser-fields]]
            [uap-clj.os :refer [extract-os-fields]]
            [uap-clj.device :refer [extract-device-fields]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]])
  (:gen-class))

(defn lookup-useragent
  "Takes a line with a raw useragent string and does
   browser, O/S, and device lookup
  "
  [line]
  (let [browser (extract-browser-fields line)
        os (extract-os-fields line)
        device (extract-device-fields line)]
    {:ua line :browser browser :os os :device device}))

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
