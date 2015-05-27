(ns uap-clj.core-spec
  (:require [speclj.core :refer :all]
            [uap-clj.core :refer :all]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def tests-browser (:test_cases
                     (parse-string
                       (slurp (io/resource "tests/test_ua.yaml")))))
(def tests-os (:test_cases
                (parse-string
                  (slurp (io/resource "tests/test_os.yaml")))))
(def tests-device (:test_cases
                    (parse-string
                      (slurp (io/resource "tests/test_device.yaml")))))

(def partitioned-tests-device (partition 4000 4000 [nil] tests-device))

(def unknown-ua "Crazy new useragent in the wild/v0.1.0")

(defn run-browser-fixture
  "Assert match between fixture test data:
     UserAgent: family/major/minor/patch
   and output of parser function
  "
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        browser (extract-browser-fields line regexes)]
  (do-template [family major minor patch]
               (describe (format "a user agent in the '%s' browser family" (str family))
                 (it (format "is in the '%s' browser family" (str family))
                   (should= (str family) (str (:family browser))))
                 (it (format "has '%s' as its major number" (str major))
                   (should= (str major) (str (:major browser))))
                 (it (format "has '%s' as its minor number" (str minor))
                   (should= (str minor) (str (:minor browser))))
                 (it (format "has '%s' as its patch number" (str patch))
                   (should= (str patch) (str (:patch browser)))))
               (get expected :family "")
               (get expected :major "")
               (get expected :minor "")
               (get expected :patch ""))))

(defn run-os-fixture
  "Assert match between fixture test data:
     OS: family/major/minor/patch/patch_minor
   and output of parser function
  "
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch :patch_minor])
        os (extract-os-fields line regexes)]
  (do-template [family major minor patch patch-minor]
               (describe (format "a user agent in the '%s' O/S family" (str family))
                 (it (format "is in the '%s' O/S family" (str family))
                   (should= (str family) (str (:family os))))
                 (it (format "has '%s' as its major number" (str major))
                   (should= (str major) (str (:major os))))
                 (it (format "has '%s' as its minor number" (str minor))
                   (should= (str minor) (str (:minor os))))
                 (it (format "has '%s' as its patch number" (str patch))
                   (should= (str patch) (str (:patch os))))
                 (it (format "has '%s' as its patch_minor number" (str patch-minor))
                   (should= (str patch-minor) (str (:patch_minor os)))))
               (get expected :family "")
               (get expected :major "")
               (get expected :minor "")
               (get expected :patch "")
               (get expected :patch_minor ""))))

(defn run-device-fixture
  "Assert match between fixture test data:
     Device: family/brand/model
   and output of parser function
  "
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :brand :model])
        device (extract-device-fields line regexes)]
  (do-template [family brand model]
               (describe (format "a user agent in the '%s' Device family" (str family))
                 (it (format "is in the '%s' Device family" (str family))
                   (should= (str family) (str (:family device))))
                 (it (format "has '%s' as its brand" (str brand))
                   (should= (str brand) (str (:brand device))))
                 (it (format "has '%s' as its model" (str model))
                   (should= (str model) (str (:model device)))))
               (get expected :family "")
               (get expected :brand "")
               (get expected :model ""))))

(context "Known Browsers:"
  (map #(run-browser-fixture % regexes-browser) tests-browser))

(context "Known O/S:"
  (map #(run-os-fixture % regexes-os) tests-os))

;;;
;;; As of this commit, there are a very large number of tests in test_device.yaml:
;;;   uap-clj.core=> (count tests-device)
;;;   15948
;;; These test fixtures are generated at compile time; exceeding around 4200 tests
;;;   blows the stack of the thread running the tests in a context below (on the
;;;   developer's machine), but partitioning as below works around that annoyance.
;;;   (A case could be made for setting "-Xss" in LEIN_JVM_OPTS, but I wanted to
;;;   minimize environmental dependencies in my attempt to make this work.)
;;;
(context "Known Devices:"
  (context "Part 1 of 4:"
    (map #(run-device-fixture % regexes-device)
         (first partitioned-tests-device)))
  (context "Part 2 of 4:"
    (map #(run-device-fixture % regexes-device)
         (second partitioned-tests-device)))
  (context "Part 3 of 4:"
    (map #(run-device-fixture % regexes-device)
         (nth partitioned-tests-device 2)))
  (context "Part 4 of 4:"
    (map #(run-device-fixture % regexes-device)
         (butlast (last partitioned-tests-device)))))

;;;
;;; The ua-parser core specification requires setting browser family to "Other"
;;;   and major, minor, & patch numbers to nothing if an unfamiliar
;;;   (i.e. not in regexes.yaml) useragent string is encountered.
;;;
(context "Unknown browser"
  (let [browser (extract-browser-fields unknown-ua regexes-browser)]
    (describe (format "An as-yet uncataloged browser '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (:family browser))))
      (it "has '' as its major number"
        (should= "" (str (:major browser))))
      (it "has '' as its minor number"
        (should= "" (str (:minor browser))))
      (it "has '' as its patch number"
        (should= "" (str (:patch browser)))))))

;;;
;;; The ua-parser core specification requires setting o/s family to "Other"
;;;   and major, minor, patch, and patch minor numbers to nothing if an unfamiliar
;;;   (i.e. not in regexes.yaml) useragent string is encountered.
;;;
(context "Unknown o/s"
  (let [os (extract-os-fields unknown-ua regexes-os)]
    (describe (format "An as-yet uncataloged o/s '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (:family os))))
      (it "has '' as its major number"
        (should= "" (str (:major os))))
      (it "has '' as its minor number"
        (should= "" (str (:minor os))))
      (it "has '' as its patch number"
        (should= "" (str (:patch os))))
      (it "has '' as its patch-minor number"
        (should= "" (str (:patch_minor os)))))))

;;;
;;; The ua-parser core specification requires setting device family to "Other"
;;;   and brand & model to nothing if an unfamiliar (i.e. not in regexes.yaml)
;;;   useragent string is encountered.
;;;
(context "Unknown device"
  (let [device (extract-device-fields unknown-ua regexes-device)]
    (describe (format "An as-yet uncataloged device '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (:family device))))
      (it "has '' as its brand"
        (should= "" (str (:brand device))))
      (it "has '' as its model"
        (should= "" (str (:model device)))))))
