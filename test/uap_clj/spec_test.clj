(ns uap-clj.spec-test
  "Tests for clojure.spec definitions"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer [deftest is testing]]
   [uap-clj.browser :refer [browser]]
   [uap-clj.core :refer [useragent]]
   [uap-clj.device :refer [device]]
   [uap-clj.os :refer [os]]
   [uap-clj.spec]))


(def chrome-ua
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")


(def unknown-ua
  "Unknown new useragent in the wild/v0.1.0")


(deftest browser-spec-test
  (testing "known browser conforms to ::browser spec"
    (is (s/valid? :uap-clj.spec/browser (browser chrome-ua))))
  (testing "unknown browser conforms to ::browser spec"
    (is (s/valid? :uap-clj.spec/browser (browser unknown-ua))))
  (testing "nil input conforms to ::browser spec"
    (is (s/valid? :uap-clj.spec/browser (browser nil)))))


(deftest os-spec-test
  (testing "known OS conforms to ::os spec"
    (is (s/valid? :uap-clj.spec/os (os chrome-ua))))
  (testing "unknown OS conforms to ::os spec"
    (is (s/valid? :uap-clj.spec/os (os unknown-ua)))))


(deftest device-spec-test
  (testing "known device conforms to ::device spec"
    (is (s/valid? :uap-clj.spec/device (device chrome-ua))))
  (testing "unknown device conforms to ::device spec"
    (is (s/valid? :uap-clj.spec/device (device unknown-ua)))))


(deftest useragent-spec-test
  (testing "full useragent result conforms to ::useragent spec"
    (is (s/valid? :uap-clj.spec/useragent (useragent chrome-ua))))
  (testing "unknown useragent conforms to ::useragent spec"
    (is (s/valid? :uap-clj.spec/useragent (useragent unknown-ua)))))
