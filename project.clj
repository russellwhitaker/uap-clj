(defproject uap-clj "1.5.0"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure      "1.11.1"]
                 [levand/immuconf          "0.1.0"]
                 [clj-commons/clj-yaml     "0.7.109"]]
  :jar-exclusions [#"dev_resources|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]
  :profiles {:dev
              {:dependencies [[criterium "0.4.6"]
                              [speclj    "3.4.1"]]
               :jvm-opts ["-Xss256M"]
               :test-paths ["spec"]}
             :uberjar {:uberjar-exclusions
                        [#"dev_resources|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]}}
  :plugins [[lein-ancient  "0.6.15"]
            [speclj        "3.4.1"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths ["src/resources/submodules" "src/resources/submodules/tests"]
  :main ^:skip-aot uap-clj.core
  :aliases {"test"  ["do" ["clean"] ["spec" "--reporter=d"]]
            "build" ["do" ["clean"] ["uberjar"]]})
