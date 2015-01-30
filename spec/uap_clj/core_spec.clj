(ns uap-clj.core-spec
  (:require [speclj.core :refer :all]
            [uap-clj.core :refer :all]
            [clj-yaml.core :refer [parse-string]]
            [clojure.template :refer [do-template]]))

(def tests-browser (:test_cases
                     (parse-string
                       (slurp ".lein-git-deps/uap-core/tests/test_ua.yaml"))))
(def tests-os (:test_cases
                (parse-string
                  (slurp ".lein-git-deps/uap-core/tests/test_os.yaml"))))

(defn run-browser-fixture
  "Assert match between fixture test data:
     UserAgent: family/major/minor/patch
   and output of parser function"
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        browser (extract-browser-fields line regexes)]
  (do-template [family major minor patch]
               (describe (format "a user agent in the %s browser family" family)
                 (it (format "is in the %s browser family" family)
                   (should= family (:family browser)))
                 (it (format "has %s as its major number" major)
                   (should= major (:major browser)))
                 (it (format "has %s as its minor number" minor)
                   (should= minor (:minor browser)))
                 (it (format "has %s as its patch number" patch)
                   (should= patch (:patch browser))))
               (:family expected) (:major expected) (:minor expected) (:patch expected))))

(defn run-os-fixture
  "Assert match between fixture test data:
     OS: family/major/minor/patch/patch_minor
   and output of parser function"
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        os (extract-os-fields line regexes)]
  (do-template [family major minor patch]
               (describe (format "a user agent in the %s O/S family" family)
                 (it (format "is in the %s O/S family" family)
                   (should= family (:family os)))
                 (it (format "has %s as its major number" major)
                   (should= major (:major os)))
                 (it (format "has %s as its minor number" minor)
                   (should= minor (:minor os)))
                 (it (format "has %s as its patch number" patch)
                   (should= patch (:patch os))))
               (:family expected) (:major expected) (:minor expected) (:patch expected))))

(context "Browser"
  (map #(run-browser-fixture % regexes-browser)
    (map #(select-keys % [:user_agent_string :family :major :minor :patch])
         tests-browser)))

(context "O/S"
  (map #(run-os-fixture % regexes-os)
    (map #(select-keys % [:user_agent_string :family :major :minor :patch])
         tests-os)))

(run-specs)
