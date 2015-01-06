(ns uap-clj.core-spec
  (:require [speclj.core :refer :all]
            [uap-clj.core :refer :all]
            [clj-yaml.core :refer [parse-string]]
            [clojure.template :refer [do-template]]))

(def tests-all (:test_cases (parse-string (slurp ".lein-git-deps/uap-core/tests/test_ua.yaml"))))

(defn run-ua-fixture
  "Assert match between fixture test user agent family/major/minor/patch and output of parser function"
  [fixture regexes]
  (let [expected (:family fixture)
        line (:user_agent_string fixture)]
  (do-template [expected line]
               (describe (format "a %s user agent" expected)
                 (it (format "is %s" expected)
                   (should= expected
                            (get-user-agent line regexes))))
               expected line)))

(context "User agents"
  (map #(run-ua-fixture % regexes-ua)
    (map #(select-keys % [:user_agent_string :family]) tests-all)))
