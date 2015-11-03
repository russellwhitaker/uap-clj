(ns uap-clj.device-spec
  (:require [speclj.core :refer :all]
            [uap-clj.device :refer :all]
            [uap-clj.common-spec :refer [unknown-ua]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def tests-device (:test_cases
                    (parse-string
                      (slurp (io/resource "tests/test_device.yaml")))))

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
               (describe (format "a user agent '%s' in the '%s' Device family" line (str family))
                 (it (format "is in the '%s' Device family" (str family))
                   (should= (str family) (str (:family device))))
                 (it (format "has '%s' as its brand" (str brand))
                   (should= (str brand) (str (:brand device))))
                 (it (format "has '%s' as its model" (str model))
                   (should= (str model) (str (:model device)))))
               (get expected :family "")
               (get expected :brand "")
               (get expected :model ""))))

(context "Known Device:"
  (map #(run-device-fixture % regexes-device) tests-device))

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
