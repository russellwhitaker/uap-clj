(defproject uap-clj "1.0.2"
  :description "Clojure language implementation of ua-parser"
  :url "https://github.com/russellwhitaker/uap-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :scm {:name "git"
        :url "https://github.com/russellwhitaker/uap-clj"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [circleci/clj-yaml "0.5.3"]]
  :jvm-opts ["-Xss2m"]
  :profiles {:dev
              {:dependencies [[speclj "3.3.1"]
                              [lein-git-deps "0.0.2"]]}}
  :plugins [[lein-git-deps "0.0.2"]
            [speclj "3.3.1"]]
  :test-paths ["spec"]
  :git-dependencies [["https://github.com/ua-parser/uap-core.git"]]
  :resource-paths [".lein-git-deps/uap-core"]
  :main uap-clj.core
  :aot :all)
