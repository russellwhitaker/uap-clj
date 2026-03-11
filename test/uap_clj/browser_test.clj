(ns uap-clj.browser-test
  "Test suite for browser lookup functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [uap-clj.browser :refer [browser]]
            [uap-clj.test-helpers :refer [unknown-ua load-fixture]]))

(def tests (load-fixture "test_ua.yaml"))

(deftest known-browsers-test
  (doseq [fixture tests]
    (let [line (:user_agent_string fixture)
          expected (select-keys fixture [:family :major :minor :patch])
          result (browser line)]
      (testing (format "UA: %s" line)
        (is (= (str (get expected :family "")) (str (:family result))))
        (is (= (str (get expected :major "")) (str (:major result))))
        (is (= (str (get expected :minor "")) (str (:minor result))))
        (is (= (str (get expected :patch "")) (str (:patch result))))))))

(deftest nil-input-test
  (testing "graceful handling of nil input"
    (is (= {:family nil :major nil :minor nil :patch nil}
           (browser nil)))))

(deftest unknown-browser-test
  (testing (format "An as-yet uncataloged browser '%s'" unknown-ua)
    (let [result (browser unknown-ua)]
      (is (= "Other" (str (:family result))))
      (is (= "" (str (:major result))))
      (is (= "" (str (:minor result))))
      (is (= "" (str (:patch result)))))))
