(ns uap-clj.java.api.browser-test
  "Test suite for Java API to browser lookup functionality"
  (:require
   [clojure.test :refer [deftest is testing]]
   [uap-clj.java.api.browser :refer [-lookup]]
   [uap-clj.test-helpers :refer [unknown-ua load-fixture]]))


(def tests (load-fixture "test_ua.yaml"))


(deftest known-browsers-test
  (doseq [fixture tests]
    (let [line (:user_agent_string fixture)
          expected (select-keys fixture [:family :major :minor :patch])
          result (-lookup line)]
      (testing (format "UA: %s" line)
        (is (= (str (get expected :family "")) (str (.get result "family"))))
        (is (= (str (get expected :major "")) (str (.get result "major"))))
        (is (= (str (get expected :minor "")) (str (.get result "minor"))))
        (is (= (str (get expected :patch "")) (str (.get result "patch"))))))))


(deftest unknown-browser-test
  (testing (format "An as-yet uncataloged browser '%s'" unknown-ua)
    (let [result (-lookup unknown-ua)]
      (is (= "Other" (str (.get result "family"))))
      (is (= "" (str (.get result "major"))))
      (is (= "" (str (.get result "minor"))))
      (is (= "" (str (.get result "patch")))))))
