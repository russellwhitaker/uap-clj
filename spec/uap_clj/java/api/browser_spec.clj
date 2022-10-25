(ns uap-clj.java.api.browser-spec
  "Test suite for Java API to browser lookup functionality"
  (:require [speclj.core :refer :all]
            [uap-clj.java.api.browser :refer [-lookup]]
            [uap-clj.common-spec :refer [unknown-ua load-fixture]]
            [clojure.template :refer [do-template]]))

(def tests (load-fixture "tests/test_ua.yaml"))

(defn run-browser-fixture
  "Assert match between fixture test data:
     UserAgent: family/major/minor/patch
   and output of parser function
  "
  [fixture]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        browser (-lookup line)]
  (do-template [family major minor patch]
               (describe
                 (format
                   "a user agent '%s' in the '%s' browser family"
                   line (str family))
                 (it (format "is in the '%s' browser family" (str family))
                   (should= (str family) (str (.get browser "family"))))
                 (it (format "has '%s' as its major number" (str major))
                   (should= (str major) (str (.get browser "major"))))
                 (it (format "has '%s' as its minor number" (str minor))
                   (should= (str minor) (str (.get browser "minor"))))
                 (it (format "has '%s' as its patch number" (str patch))
                   (should= (str patch) (str (.get browser "patch")))))
               (get expected :family "")
               (get expected :major "")
               (get expected :minor "")
               (get expected :patch ""))))

(context "Known Browsers:"
  (map #(run-browser-fixture %) tests))

;;;
;;; The ua-parser core specification requires setting browser family to "Other"
;;;   and major, minor, & patch numbers to nothing if an unfamiliar
;;;   (i.e. not in regexes.yaml) useragent string is encountered.
;;;
(context "Unknown browser"
  (let [browser (-lookup unknown-ua)]
    (describe (format "An as-yet uncataloged browser '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (.get browser "family"))))
      (it "has '' as its major number"
        (should= "" (str (.get browser "major"))))
      (it "has '' as its minor number"
        (should= "" (str (.get browser "minor"))))
      (it "has '' as its patch number"
        (should= "" (str (.get browser "patch")))))))
