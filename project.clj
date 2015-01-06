(defproject uap-clj "0.1.0-SNAPSHOT"
  :description "Clojure language implementation of ua-parser"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [circleci/clj-yaml "0.5.3"]]
  :profiles {:dev {:dependencies [[speclj "3.1.0"]
                                  [lein-git-deps "0.0.2-SNAPSHOT"]]}}
  :plugins [[lein-git-deps "0.0.2-SNAPSHOT"]
            [speclj "3.1.0"]]
  :test-paths ["spec"]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]])

