(ns uap-clj.java.api.device-test
  "Test suite for Java API to device lookup functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [uap-clj.java.api.device :refer [-lookup]]
            [uap-clj.test-helpers :refer [unknown-ua load-fixture]]))

(def tests (load-fixture "test_device.yaml"))

(deftest known-devices-test
  (doseq [fixture tests]
    (let [line (:user_agent_string fixture)
          expected (select-keys fixture [:family :brand :model])
          result (-lookup line)]
      (testing (format "UA: %s" line)
        (is (= (str (get expected :family "")) (str (.get result "family"))))
        (is (= (str (get expected :brand "")) (str (.get result "brand"))))
        (is (= (str (get expected :model "")) (str (.get result "model"))))))))

(deftest unknown-device-test
  (testing (format "An as-yet uncataloged device '%s'" unknown-ua)
    (let [result (-lookup unknown-ua)]
      (is (= "Other" (str (.get result "family"))))
      (is (= "" (str (.get result "brand"))))
      (is (= "" (str (.get result "model")))))))
