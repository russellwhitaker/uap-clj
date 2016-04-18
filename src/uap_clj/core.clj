(ns uap-clj.core
  "Core library with entrypoint main function"
  (:refer-clojure :exclude [load])
  (:require [immuconf.config :as conf :refer [load]]
            [uap-clj.conf :refer [base-config
                                  local-config]]
            [uap-clj.common :as common :refer :all]
            [uap-clj.browser :refer [browser]]
            [uap-clj.os :refer [os]]
            [uap-clj.device :refer [device]]
            [clj-yaml.core :refer [parse-string]]
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

(def useragent
  (memoize
    (fn
      [line]
      {:ua line
       :browser (browser line)
       :os (os line)
       :device (device line)})))

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
