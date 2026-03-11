(ns uap-clj.device-test
  "Test suite for device lookup functionality"
  (:require [clojure.test :refer [deftest is testing]]
            [uap-clj.device :refer [device]]
            [uap-clj.test-helpers :refer [unknown-ua load-fixture]]))

(def tests (load-fixture "test_device.yaml"))

(deftest known-devices-test
  (doseq [fixture tests]
    (let [line (:user_agent_string fixture)
          expected (select-keys fixture [:family :brand :model])
          result (device line)]
      (testing (format "UA: %s" line)
        (is (= (str (get expected :family "")) (str (:family result))))
        (is (= (str (get expected :brand "")) (str (:brand result))))
        (is (= (str (get expected :model "")) (str (:model result))))))))

(deftest nil-input-test
  (testing "graceful handling of nil input"
    (is (= {:family nil :brand nil :model nil}
           (device nil)))))

(deftest unknown-device-test
  (testing (format "An as-yet uncataloged device '%s'" unknown-ua)
    (let [result (device unknown-ua)]
      (is (= "Other" (str (:family result))))
      (is (= "" (str (:brand result))))
      (is (= "" (str (:model result)))))))
