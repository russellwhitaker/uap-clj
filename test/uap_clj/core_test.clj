(ns uap-clj.core-test
  "Tests for limited bad input edge cases for core
   'useragent' function. Exhaustive (generative) testing
   for the os, device, and browser functions - which 'useragent'
   calls - are found in their own tests."
  (:require [clojure.test :refer [deftest is testing]]
            [uap-clj.test-helpers :refer [unknown-ua]]
            [uap-clj.core :refer [useragent]]))

(deftest nil-input-test
  (testing "graceful handling of nil input"
    (is (= {:ua nil
            :browser
              {:family nil
               :major nil
               :minor nil
               :patch nil}
            :os
              {:family nil
               :major nil
               :minor nil
               :patch nil
               :patch_minor nil}
            :device
              {:family nil
               :brand nil
               :model nil}}
           (useragent nil)))))

(deftest unknown-useragent-test
  (testing "graceful handling returns 'Other' classification"
    (is (= {:ua "Unknown new useragent in the wild/v0.1.0"
            :browser
              {:family "Other"
               :major nil
               :minor nil
               :patch nil}
            :os
              {:family "Other"
               :major nil
               :minor nil
               :patch nil
               :patch_minor nil}
            :device
              {:family "Other"
               :brand nil
               :model nil}}
           (useragent unknown-ua)))))
