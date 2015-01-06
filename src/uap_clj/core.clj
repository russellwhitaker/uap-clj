(ns uap-clj.core
  (:require [clj-yaml.core :refer [parse-string]]))

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
