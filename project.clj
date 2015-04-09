(defproject uap-clj "0.2.0"
  :description "Clojure language implementation of ua-parser with Hive UDF wrappers"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [circleci/clj-yaml "0.5.3"]
                 [org.apache.hive/hive-exec "0.12.0"]
                 [org.apache.hive/hive-serde "0.12.0"]
                 [org.apache.hadoop/hadoop-core "1.2.1"]]
  :profiles {:dev {:dependencies [[speclj "3.2.0"]
                                  [lein-git-deps "0.0.2-SNAPSHOT"]]}}
  :plugins [[lein-git-deps "0.0.2-SNAPSHOT"]
            [speclj "3.2.0"]]
  :test-paths ["spec"]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main uap-clj.core
  :aot :all)
