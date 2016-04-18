(ns uap-clj.core
  "Core library with entrypoint main function"
  (:require [uap-clj.common :as common :refer :all]
            [uap-clj.browser :refer [browser]]
            [uap-clj.os :refer [os]]
            [uap-clj.device :refer [device]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]]))

(def useragent
  (memoize
    (fn
      [line]
      {:ua line
       :browser (browser line)
       :os (os line)
       :device (device line)})))

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
                    (map useragent (line-seq rdr)))]
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
