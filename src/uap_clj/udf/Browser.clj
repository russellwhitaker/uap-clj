(ns uap-clj.udf.browser
  (:import [org.apache.hadoop.hive.ql.exec UDF]
           [org.apache.hadoop.io Text])
  (:require [uap-clj.core :refer [extract-browser-fields regexes-browser]])
  (:gen-class
   :name uap-clj.udf.Browser
   :extends org.apache.hadoop.hive.ql.exec.UDF
   :methods [[evaluate [org.apache.hadoop.io.Text] org.apache.hadoop.io.Text]]))

(defn #^Text -evaluate
  "Extract Browser family<tab>major number<tab>minor number<tab>patch
   from a useragent string

   USAGE: after adding the .jar file from this project, create your temporary function
     (e.g. as temporary function browser()), then use Hive split(browser(agentstring), '\\t')
     to break into 4 fields in your query, returning a Hive array<string> of length 4 which
     can be stored in an array<string> field and later referenced in a hive query by index, e.g.:

     SELECT browser[0] AS browser_family,
            browser[1] AS browser_major,
            browser[2] AS browser_minor,
            browser[3] AS browser_patch
     FROM parsed_useragent;
  "
  [this #^Text s]
  (when s
    (Text.
      (try
        (let [ua (extract-browser-fields (.toString s) regexes-browser)]
          (str (or (get ua :family nil) "<empty>") \tab
               (or (get ua :major nil) "<empty>") \tab
               (or (get ua :minor nil) "<empty>") \tab
               (or (get ua :patch nil) "<empty>")))
      (catch Exception e (str (.getMessage e) ": " (.toString s)))))))
