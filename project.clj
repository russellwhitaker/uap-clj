(defproject uap-clj "1.1.1"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [circleci/clj-yaml "0.5.3"]]
  :profiles {:dev
              {:dependencies [[speclj "3.3.1"]
                              [lein-git-deps "0.0.2"]]
               :test-paths ["spec"]
               :jvm-opts ["-Xss256M"]}
             :uberjar {:aot :all
                       :uberjar-exclusions
                         [#"tests|test_resources|docs|\.md|LICENSE|META-INF"]}}
  :plugins [[lein-git-deps "0.0.2"]
            [speclj "3.3.1"]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main uap-clj.core
  :aot :all)
