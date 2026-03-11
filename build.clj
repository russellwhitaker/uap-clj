(ns build
  (:require [clojure.tools.build.api :as b])
  (:refer-clojure :exclude [test]))

(def lib 'uap-clj/uap-clj)
(def version "1.8.1")
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))

(def basis (delay (b/create-basis {:project "deps.edn"})))

(def pom-data
  [[:description "Clojure language implementation of ua-parser"]
   [:url "https://github.com/russellwhitaker/uap-clj"]
   [:licenses
    [:license
     [:name "The MIT License (MIT)"]
     [:url "http://www.opensource.org/licenses/mit-license.php"]]]
   [:scm
    [:url "https://github.com/russellwhitaker/uap-clj"]
    [:connection "scm:git:https://github.com/russellwhitaker/uap-clj.git"]
    [:developerConnection "scm:git:ssh://git@github.com/russellwhitaker/uap-clj.git"]
    [:tag (str "v" version)]]])

(defn clean
  "Delete the target directory.
   Usage: clojure -T:build clean"
  [_]
  (b/delete {:path "target"}))

(defn compile-java
  "AOT compile gen-class namespaces for the Java API.
   Usage: clojure -T:build compile-java"
  [_]
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile ['uap-clj.java.api.browser
                               'uap-clj.java.api.device
                               'uap-clj.java.api.os]}))

(defn jar
  "Build the library JAR for deployment to Clojars.
   Usage: clojure -T:build jar"
  [_]
  (clean nil)
  (compile-java nil)
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]
                :pom-data pom-data})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir
               :include "**/*.{clj,cljc}"})
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir
               :include "**.edn"})
  (b/copy-dir {:src-dirs ["src/resources"]
               :target-dir (str class-dir "/resources")
               :include "**.edn"})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (println "Built" jar-file))

(defn uber
  "Build a standalone uberjar.
   Usage: clojure -T:build uber"
  [_]
  (clean nil)
  (compile-java nil)
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir
               :include "**/*.{clj,cljc}"})
  (b/copy-dir {:src-dirs ["resources"]
               :target-dir class-dir
               :include "**.edn"})
  (b/copy-dir {:src-dirs ["src/resources"]
               :target-dir (str class-dir "/resources")
               :include "**.edn"})
  (b/compile-clj {:basis @basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'uap-clj.core})
  (println "Built" uber-file))

(defn deploy
  "Deploy the library JAR to Clojars.
   Requires CLOJARS_USERNAME and CLOJARS_PASSWORD env vars.
   Usage: clojure -T:build deploy"
  [_]
  (jar nil)
  ((requiring-resolve 'deps-deploy.deps-deploy/deploy)
   {:installer :remote
    :artifact (b/resolve-path jar-file)
    :pom-file (b/pom-path {:lib lib :class-dir class-dir})
    :sign-releases? false}))

(defn test
  "Run the clojure.test suite via Cognitect test-runner.
   Usage: clojure -T:build test"
  [_]
  (let [proc (-> (ProcessBuilder. ["clojure" "-M:test"])
                 (.inheritIO)
                 (.start))]
    (when-not (zero? (.waitFor proc))
      (throw (ex-info "Tests failed" {:exit (.exitValue proc)})))))

(defn tag
  "Create a git tag for the current version (e.g. v1.8.1).
   Fails if the tag already exists.
   Usage: clojure -T:build tag"
  [_]
  (let [tag (str "v" version)
        result (b/process {:command-args ["git" "tag" "-a" tag "-m" (str "Release " tag)]
                           :dir "."})]
    (when-not (zero? (:exit result))
      (throw (ex-info (str "Failed to create tag " tag) result)))
    (println "Created tag" tag)))

(defn release
  "Build, deploy to Clojars, and create a git tag.
   Requires CLOJARS_USERNAME and CLOJARS_PASSWORD env vars.
   After running, push the tag with: git push origin v<version>
   Usage: clojure -T:build release"
  [_]
  (deploy nil)
  (tag nil)
  (println (str "\nRelease " version " complete."
                "\nPush the tag with: git push origin v" version)))

(defn outdated
  "Check for outdated dependencies. Wraps antq.
   Usage: clojure -T:build outdated"
  [opts]
  (try
    ((requiring-resolve 'antq.tool/outdated)
     (merge {:check-clojure-tools true} opts))
    (catch clojure.lang.ExceptionInfo e
      (when-not (= "Exited" (.getMessage e))
        (throw e)))))
