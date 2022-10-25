(ns uap-clj.common-spec
  (:require [clojure.java.io :as io :refer [resource]]
            [clj-yaml.core :refer [parse-string]])
  (:import [org.yaml.snakeyaml LoaderOptions]))

(def ^:const unknown-ua "Unknown new useragent in the wild/v0.1.0")

(defn default-codepoint-limit
  []
  (let [options (LoaderOptions.)]
    (.getCodePointLimit options)))

(defn snakeyaml-loader-options
  ^LoaderOptions [n]
  (let [options (LoaderOptions.)]
    (.setCodePointLimit options n)
    options))

(defn load-fixture
  [f]
  (let [raw-file (slurp (io/resource f))
        file-size (count raw-file)]
    (if (>= file-size (default-codepoint-limit))
      (:test_cases
        (with-redefs [clj-yaml.core/default-loader-options #(snakeyaml-loader-options file-size)]
          (parse-string raw-file)))
      (:test_cases (parse-string raw-file)))))
