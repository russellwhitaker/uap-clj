(ns uap-clj.core-spec
  (:require [speclj.core :refer :all]
            [uap-clj.core :refer :all]
            [clj-yaml.core :refer [parse-string]]
            [clojure.template :refer [do-template]]))

(def tests-all (:test_cases
                 (parse-string
                   (slurp ".lein-git-deps/uap-core/tests/test_ua.yaml"))))

(defn run-ua-fixture
  "Assert match between fixture test user agent family/major/minor/patch
   and output of parser function"
  [fixture regexes]
  (let [line (:user_agent_string fixture)
        expected (select-keys fixture [:family :major :minor :patch])
        ua (get-user-agent line regexes)]
  (do-template [family major minor patch]
               (describe (format "a user agent in the %s family" family)
                 (it (format "is in the %s family" family)
                   (should= family (:family ua)))
                 (it (format "has %s as its major number" major)
                   (should= major (:major ua)))
                 (it (format "has %s as its minor number" minor)
                   (should= minor (:minor ua)))
                 (it (format "has %s as its patch number" patch)
                   (should= patch (:patch ua))))
               (:family expected) (:major expected) (:minor expected) (:patch expected))))

(context "User agents"
  (map #(run-ua-fixture % regexes-ua)
    (map #(select-keys % [:user_agent_string :family :major :minor :patch])
         tests-all)))
