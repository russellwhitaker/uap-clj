(ns uap-clj.os-test
  "Test suite for o/s lookup functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [uap-clj.os :refer [os]]
            [uap-clj.test-helpers :refer [unknown-ua load-fixture]]))

(def tests (load-fixture "test_os.yaml"))

(deftest known-os-test
  (doseq [fixture tests]
    (let [line (:user_agent_string fixture)
          expected (select-keys fixture [:family :major :minor :patch :patch_minor])
          result (os line)]
      (testing (format "UA: %s" line)
        (is (= (str (get expected :family "")) (str (:family result))))
        (is (= (str (get expected :major "")) (str (:major result))))
        (is (= (str (get expected :minor "")) (str (:minor result))))
        (is (= (str (get expected :patch "")) (str (:patch result))))
        (is (= (str (get expected :patch_minor "")) (str (:patch_minor result))))))))

(deftest nil-input-test
  (testing "graceful handling of nil input"
    (is (= {:family nil :major nil :minor nil :patch nil :patch_minor nil}
           (os nil)))))

(deftest unknown-os-test
  (testing (format "An as-yet uncataloged o/s '%s'" unknown-ua)
    (let [result (os unknown-ua)]
      (is (= "Other" (str (:family result))))
      (is (= "" (str (:major result))))
      (is (= "" (str (:minor result))))
      (is (= "" (str (:patch result))))
      (is (= "" (str (:patch_minor result)))))))
