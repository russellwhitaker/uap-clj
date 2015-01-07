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
  (let [matcher (re-matcher (re-pattern (:regex regex)) line)
        result (re-find matcher)]
    {:result result :matcher matcher :regex regex}))

(defn get-user-agent
  "Extract browser family from user agent string"
  [ua regexes]
  (let [result-scan (map #(match-with-context ua %) regexes)
        first-hit (first (remove #(= nil (:result %)) result-scan))]
    (or
      (get-in first-hit [:regex :family_replacement]
              (second (get first-hit :result)))
      "Other")))

(defn -main
  "Takes a filename from the commandline containing one useragent per line,
   and prints useragent with its matching browser family separated by a tab
   character."
  [filename]
  (with-open [useragents (clojure.java.io/reader filename)]
    (let [lookup-results (map vector (line-seq useragents)
                                     (map #(get-user-agent % regexes-ua)
                                          (line-seq useragents)))]
      (doseq [x lookup-results]
        (println (str (first x) \tab (second x)))))))
