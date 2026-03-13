(ns build
  (:refer-clojure :exclude [test])
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]))


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


(def native-image-name (format "target/%s" (name lib)))


(defn native-image
  "Build a native binary using GraalVM native-image.
   Requires GraalVM with native-image installed.
   Usage: clojure -T:build native-image"
  [_]
  (uber nil)
  (let [result (b/process
                {:command-args ["native-image"
                                "-jar" uber-file
                                "-o" native-image-name
                                "--no-fallback"
                                "-H:+ReportExceptionStackTraces"
                                "--initialize-at-build-time"
                                "-H:Log=registerResource:"
                                "--enable-url-protocols=http,https"]
                 :dir "."
                 :inherit true})]
    (when-not (zero? (:exit result))
      (throw (ex-info "native-image build failed" result))))
  (println "Built" native-image-name))


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
                           :dir "."
                           :inherit true})]
    (when-not (zero? (:exit result))
      (throw (ex-info (str "Failed to create tag " tag) result)))
    (println "Created tag" tag)))


(defn- github-repo
  "Derive owner/repo from the origin remote URL."
  []
  (let [result (b/process {:command-args ["git" "remote" "get-url" "origin"]
                           :dir "."})]
    (when-not (zero? (:exit result))
      (throw (ex-info "Failed to get origin remote URL" result)))
    (let [url (str/trim (:out result))]
      (second (re-find #"github\.com[:/](.+?)(?:\.git)?$" url)))))


(defn release
  "Build, deploy to Clojars, create a git tag, push it, and create a GitHub Release.
   Requires CLOJARS_USERNAME and CLOJARS_PASSWORD env vars, and gh CLI.
   Usage: clojure -T:build release"
  [_]
  (let [release-tag (str "v" version)
        repo (github-repo)]
    ;; Pre-flight: fetch remote tags and check tag doesn't exist
    (let [fetch (b/process {:command-args ["git" "fetch" "--tags" "origin"]
                            :dir "."})]
      (when-not (zero? (:exit fetch))
        (throw (ex-info "Failed to fetch tags from origin; aborting release before deploy."
                        fetch))))
    (let [check (b/process {:command-args ["git" "rev-parse" "-q" "--verify"
                                           (str "refs/tags/" release-tag)]
                            :dir "."})]
      (when (zero? (:exit check))
        (throw (ex-info (str "Tag " release-tag " already exists; aborting release before deploy.")
                        check))))
    ;; Deploy to Clojars and create local tag
    (deploy nil)
    (tag nil)
    ;; Push tag to origin
    (let [push (b/process {:command-args ["git" "push" "origin" release-tag]
                           :dir "."
                           :inherit true})]
      (when-not (zero? (:exit push))
        (throw (ex-info (str "Failed to push tag " release-tag) push))))
    ;; Create GitHub Release
    (let [prev-tag-proc (b/process {:command-args ["git" "describe" "--tags" "--abbrev=0"
                                                   (str release-tag "^")]
                                    :dir "."})
          body (if (zero? (:exit prev-tag-proc))
                 (let [prev-tag (str/trim (:out prev-tag-proc))]
                   (str "**Full Changelog**: https://github.com/" repo "/compare/"
                        prev-tag "..." release-tag))
                 (str "Release " release-tag))
          gh (b/process {:command-args ["gh" "api" (str "repos/" repo "/releases")
                                        "-X" "POST"
                                        "-f" (str "tag_name=" release-tag)
                                        "-f" (str "name=" release-tag)
                                        "-f" (str "body=" body)]
                         :dir "."
                         :inherit true})]
      (when-not (zero? (:exit gh))
        (throw (ex-info "Failed to create GitHub Release" gh))))
    (println (str "\nRelease " version " complete."))))


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
