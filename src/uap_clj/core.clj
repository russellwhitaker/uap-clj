(ns uap-clj.core
  "Core library with entrypoint main function"
  (:require [immuconf.config :as conf]
            [uap-clj.conf :refer [base-config
                                  local-config]]
            [uap-clj.common :as common :refer :all]
            [uap-clj.browser :refer [browser]]
            [uap-clj.os :refer [os]]
            [uap-clj.device :refer [device]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]]))

(defn config
  "Load & merge configuration from a path of configuration file
   sources.
  "
  []
  (apply conf/load
         (filter (partial not= nil)
                 [(base-config)
                  (local-config)])))

(defn useragent
  "Look up all 3 sets of fields for:
   - browser
   - o/s
   - device
  "
  [ua]
  {:ua ua
   :browser (browser ua)
   :os (os ua)
   :device (device ua)})

; For use in production settings where speed may be preferred
;  in exchange for the tradeoff of increased memory bloat:
(def useragent-memoized (memoize useragent))

(def cfg (config))
(def columns (:output-columns cfg))
(def header
  (str
    (s/join \tab
            (map #(s/join " " (map name (flatten %)))
                 columns))
    \newline))

(defn -main
  "Takes a filename from the commandline containing one useragent
   per line, and writes (or overwrites) a TSV (tab-separated) file
   with a header.

   If no output file is provided as an argument,
   'useragent_lookup.tsv' is the name of the default output file.
  "
  [in-file & opt-args]
  (with-open [rdr (clojure.java.io/reader in-file)]
    (let [out-file (or (first opt-args)
                       (:output-filename cfg))
          results (doall
                    (map useragent (line-seq rdr)))]
      (with-open
        [wtr (clojure.java.io/writer out-file :append false)]
        (.write wtr header)
        (doseq [ua results]
          (.write wtr
            (str (s/join \tab
                         (map #(get-in ua %) columns))
                 \newline)))))))
