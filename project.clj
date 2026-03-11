(defproject uap-clj "1.8.0"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure "1.12.4"]
                 [levand/immuconf "0.1.0"]]
  :jar-exclusions [#"dev_resources|submodules|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]
  :profiles {:dev
             {:dependencies [[criterium "0.4.6"]
                             [speclj "3.12.1"]
                             [clj-commons/clj-yaml "1.0.29"]]
              :jvm-opts ["-Xss512M"]
              :test-paths ["spec"]
              :resource-paths ["resources" "src/resources/submodules/tests"]}
             :uberjar {:uberjar-exclusions
                       [#"dev_resources|submodules|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]
                       :resource-paths ["resources"]}}
  :plugins [[com.github.liquidz/antq "2.11.1276"]
            [lein-git-deps "0.0.2"]
            [speclj "3.12.1"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :main ^:skip-aot uap-clj.core
  :aliases {"test"  ["do" ["clean"] ["spec" "--reporter=d"]]
            "build" ["do" ["clean"] ["uberjar"]]})
