(ns uap-clj.java.api.device-spec
  "Test suite for Java API to device lookup functionality"
  (:require [speclj.core :refer :all]
            [uap-clj.java.api.device :refer [-lookup]]
            [uap-clj.common-spec :refer [unknown-ua load-fixture]]
            [clojure.template :refer [do-template]]))

(def tests (load-fixture "tests/test_device.yaml"))

(defn run-device-fixture
  "Assert match between fixture test data:
     Device: family/brand/model
   and output of parser function
  "
  [fixture]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :brand :model])
        device (-lookup line)]
  (do-template [family brand model]
               (describe
                 (format "a user agent '%s' in the '%s' Device family"
                         line (str family))
                 (it (format "is in the '%s' Device family" (str family))
                   (should= (str family) (str (.get device "family"))))
                 (it (format "has '%s' as its brand" (str brand))
                   (should= (str brand) (str (.get device "brand"))))
                 (it (format "has '%s' as its model" (str model))
                   (should= (str model) (str (.get device "model")))))
               (get expected :family "")
               (get expected :brand "")
               (get expected :model ""))))

(context "Known Device:"
  (map #(run-device-fixture %) tests))

;;;
;;; The ua-parser core specification requires setting device family to "Other"
;;;   and brand & model to nothing if an unfamiliar (i.e. not in regexes.yaml)
;;;   useragent string is encountered.
;;;
(context "Unknown device"
  (let [device (-lookup unknown-ua)]
    (describe (format "An as-yet uncataloged device '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (.get device "family"))))
      (it "has '' as its brand"
        (should= "" (str (.get device "brand"))))
      (it "has '' as its model"
        (should= "" (str (.get device "model")))))))
