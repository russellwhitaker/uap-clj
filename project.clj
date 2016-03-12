(defproject uap-clj "1.1.2"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [circleci/clj-yaml "0.5.5"]]
  :profiles {:dev
              {:dependencies [[speclj "3.3.1"]
                              [lein-git-deps "0.0.2"]]
               :test-paths ["spec"]
               :jvm-opts ["-Xss256M"]}
             :uberjar {:aot :all
                       :uberjar-exclusions
                         [#"dev_resources|^test$|test_resources|docs|\.md|LICENSE"]}}
  :plugins [[lein-git-deps "0.0.2"]
            [lein-ancient "0.6.8" :exclusions [org.clojure/clojure]]
            [lein-bikeshed "0.3.0" :exclusions [org.clojure/clojure]]
            [jonase/eastwood "0.2.3" :exclusions [org.clojure/clojure]]
            [speclj "3.3.1" :exclusions [org.clojure/clojure]]]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main uap-clj.core
  :aot :all)
