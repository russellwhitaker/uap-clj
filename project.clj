(defproject uap-clj "1.3.4"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure      "1.10.1"]
                 [russellwhitaker/immuconf "0.3.0"
                   :exclusions [org.clojure/clojurescript
                                com.taoensso/timbre]]
                 [clj-commons/clj-yaml     "0.7.0"]]
  :jar-exclusions [#"dev_resources|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]
  :profiles {:dev
              {:dependencies [[criterium "0.4.5"]
                              [speclj    "3.3.2"]]
               :jvm-opts ["-Xss256M"]
               :test-paths ["spec"]}
             :uberjar {:uberjar-exclusions
                        [#"dev_resources|^test$|test_resources|tests|docs|\.md|LICENSE|package.json"]}}
  :plugins [[lein-git-deps "0.0.2"]
            [lein-ancient  "0.6.15"]
            [speclj        "3.3.2"]]
  :git-dependencies [["https://github.com/russellwhitaker/uap-core.git" "rew/fix_test_ua_yaml_tab"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main ^:skip-aot uap-clj.core
  :aliases {"test"  ["do" ["clean"] ["spec" "--reporter=d"]]
            "build" ["do" ["clean"] ["uberjar"]]})
