(ns uap-clj.browser-spec
  "Test suite for browser lookup functionality"
  (:require [speclj.core :refer :all]
            [uap-clj.browser :refer :all]
            [uap-clj.common-spec :refer [unknown-ua load-fixture]]
            [clojure.template :refer [do-template]]))

(def tests (load-fixture "test_ua.yaml"))

(defn run-browser-fixture
  "Assert match between fixture test data:
     UserAgent: family/major/minor/patch
   and output of parser function
  "
  [fixture]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        browser (browser line)]
  (do-template [family major minor patch]
               (describe
                 (format "a user agent '%s' in the '%s' browser family"
                         line (str family))
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
  (map #(run-browser-fixture %) tests))

(context "nil input"
  (describe "graceful handling"
    (it "returns default map with nil values"
      (should= {:family nil :major nil :minor nil :patch nil}
               (browser nil)))))

;;;
;;; The ua-parser core specification requires setting browser family to "Other"
;;;   and major, minor, & patch numbers to nothing if an unfamiliar
;;;   (i.e. not in regexes.yaml) useragent string is encountered.
;;;
(context "Unknown browser"
  (let [browser (browser unknown-ua)]
    (describe (format "An as-yet uncataloged browser '%s'" unknown-ua)
      (it "is categorized as family 'Other'"
        (should= "Other" (str (:family browser))))
      (it "has '' as its major number"
        (should= "" (str (:major browser))))
      (it "has '' as its minor number"
        (should= "" (str (:minor browser))))
      (it "has '' as its patch number"
        (should= "" (str (:patch browser)))))))
