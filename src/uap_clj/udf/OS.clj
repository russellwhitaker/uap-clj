(ns uap-clj.udf.os
  (:import [org.apache.hadoop.hive.ql.exec UDF]
           [org.apache.hadoop.io Text])
  (:require [uap-clj.core :refer [extract-os-fields regexes-os]])
  (:gen-class
   :name uap-clj.udf.OS
   :extends org.apache.hadoop.hive.ql.exec.UDF
   :methods [[evaluate [org.apache.hadoop.io.Text] org.apache.hadoop.io.Text]]))

(defn #^Text -evaluate
  "Extract O/S family<tab>major number<tab>minor number<tab>patch
   from a useragent string

   USAGE: after adding the .jar file from this project, create your temporary function
     (e.g. as temporary function os()), then use Hive split(os(agentstring), '\\t')
     to break into 4 fields in your query, returning a Hive array<string> of length 4 which
     can be stored in an array<string> field and later referenced in a hive query by index, e.g.:

     SELECT os[0] AS os_family,
            os[1] AS os_major,
            os[2] AS os_minor,
            os[3] AS os_patch
     FROM parsed_useragent;
  "
  [this #^Text s]
  (when s
    (Text.
      (try
        (let [ua (extract-os-fields (.toString s) regexes-os)]
          (str (or (get ua :family nil) "<empty>") \tab
               (or (get ua :major nil) "<empty>") \tab
               (or (get ua :minor nil) "<empty>") \tab
               (or (get ua :patch nil) "<empty>")))
      (catch Exception e (str (.getMessage e) ": " (.toString s)))))))
