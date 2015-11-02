(ns uap-clj.browser-spec
  (:require [speclj.core :refer :all]
            [uap-clj.core :refer :all]
            [uap-clj.common-spec :refer [unknown-ua]]
            [clj-yaml.core :refer [parse-string]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.template :refer [do-template]]))

(def tests-browser (:test_cases
                     (parse-string
                       (slurp (io/resource "tests/test_ua.yaml")))))

(defn run-browser-fixture
  "Assert match between fixture test data:
     UserAgent: family/major/minor/patch
   and output of parser function
  "
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        browser (extract-browser-fields line regexes)]
  (do-template [family major minor patch]
               (describe (format "a user agent '%s' in the '%s' browser family" line (str family))
                 (it (format "is in the '%s' browser family" (str family))
                   (should= (str family) (str (:family browser))))
                 (it (format "has '%s' as its major number" (str major))
                   (should= (str major) (str (:major browser))))
                 (it (format "has '%s' as its minor number" (str minor))
                   (should= (str minor) (str (:minor browser))))
                 (it (format "has '%s' as its patch number" (str patch))
                   (should= (str patch) (str (:patch browser)))))
               (get expected :family "")
               (get expected :major "")
               (get expected :minor "")
               (get expected :patch ""))))

(context "Known Browsers:"
  (map #(run-browser-fixture % regexes-browser) tests-browser))

;;;
;;; The ua-parser core specification requires setting browser family to "Other"
;;;   and major, minor, & patch numbers to nothing if an unfamiliar
;;;   (i.e. not in regexes.yaml) useragent string is encountered.
;;;
(context "Unknown browser"
  (let [browser (extract-browser-fields unknown-ua regexes-browser)]
    (describe (format "An as-yet uncataloged browser '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (:family browser))))
      (it "has '' as its major number"
        (should= "" (str (:major browser))))
      (it "has '' as its minor number"
        (should= "" (str (:minor browser))))
      (it "has '' as its patch number"
        (should= "" (str (:patch browser)))))))
