(defproject uap-clj "1.3.0"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure      "1.8.0"]
                 [russellwhitaker/immuconf "0.2.2"]
                 [circleci/clj-yaml        "0.5.5"]]
  :profiles {:dev
              {:dependencies [[criterium "0.4.4"]
                              [speclj    "3.3.2"]]
               :jvm-opts ["-Xss256M"]
               :test-paths ["spec"]}
             :uberjar {:aot :all
                       :uberjar-exclusions
                         [#"dev_resources|^test$|test_resources|docs|\.md|LICENSE"]}}
  :plugins [[lein-git-deps   "0.0.2"]
            [lein-ancient    "0.6.10"]
            [lein-bikeshed   "0.4.1"]
            [jonase/eastwood "0.2.3"]
            [speclj          "3.3.2"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main uap-clj.core
  :aot :all
  :aliases {"test"  ["do" ["clean"] ["spec" "--reporter=d"]]
            "build" ["do" ["clean"] ["uberjar"]]})
