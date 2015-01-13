(ns uap-clj.core
  (:require [clj-yaml.core :refer [parse-string]])
  (:gen-class))

(def regexes-all (parse-string (slurp ".lein-git-deps/uap-core/regexes.yaml")))

(def regexes-ua (:user_agent_parsers regexes-all))
(def regexes-os (:os_parsers regexes-all))
(def regexes-dev (:device_parsers regexes-all))

(defn match-with-context
  "Return match result with match groups & regex replacement map"
  [line regex]
  (let [result (re-find (re-pattern (:regex regex)) line)]
    {:result result :regex regex}))

(defn get-user-agent
  "Extract browser family, major number, minor number, and patch level
   from user agent string"
  [ua regexes]
  (let [result-scan (map #(match-with-context ua %) regexes)
        first-hit (first (remove #(= nil (:result %)) result-scan))
        result (flatten (vector (get first-hit :result)))
        family (or
                 (get-in first-hit [:regex :family_replacement]
                         (nth result 1 nil))
                 "Other")
        major (nth result 2 nil)
        minor (nth result 3 nil)
        patch (nth result 4 nil)]
    {:family family :major major :minor minor :patch patch}))

(defn -main
  "Takes a filename from the commandline containing one useragent per line,
   and prints a headerless TSV (tab-separated) file:
     useragent<tab>browser family<tab>major<tab>minor<tab>patch"
  [in-filename & optional-args]
  (with-open [rdr (clojure.java.io/reader in-filename)]
    (let [out-filename (or (first optional-args) "./output.tsv")
          results (doall
                    (map
                      (fn [line]
                        (assoc (get-user-agent line regexes-ua) :line line))
                      (line-seq rdr)))]
      (with-open [wtr (clojure.java.io/writer out-filename :append true)]
        (doseq [ua results]
          (.write wtr (str (:line ua) \tab
                           (:family ua) \tab
                           (:major ua) \tab
                           (:minor ua) \tab
                           (:patch ua) \newline)))))))
