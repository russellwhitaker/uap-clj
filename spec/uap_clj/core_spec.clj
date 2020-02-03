(ns uap-clj.core-spec
  "Tests for limited bad input edge cases for core
   'useragent' function. Exhaustive (generative) testing
   for the os, device, and browser functions - which 'useragent'
   calls - are found in their own specs.
  "
  (:require [speclj.core :refer :all]
            [uap-clj.common-spec :refer [unknown-ua]]
            [uap-clj.core :refer [useragent]]))

(context "nil input"
  (describe "graceful handling"
    (it "returns default map with nil values"
      (should= {:ua nil
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

(context "Unknown useragent"
 (describe "graceful handling"
   (it "returns default map with 'Other' classification"
     (should= {:ua "Unknown new useragent in the wild/v0.1.0"
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
