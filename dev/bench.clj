(ns bench
  "Benchmark suite for uap-clj using criterium.
   Run with: clojure -M:bench"
  (:require
   [criterium.core :as crit]
   [uap-clj.browser :refer [browser]]
   [uap-clj.core :refer [useragent]]
   [uap-clj.device :refer [device]]
   [uap-clj.os :refer [os]]))


(def sample-uas
  ["Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
   "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"
   "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.43 Mobile Safari/537.36"
   "Lenovo-A288t_TD/S100 Linux/2.6.35 Android/2.3.5 Release/02.29.2012 Browser/AppleWebkit533.1 Mobile Safari/533.1 FlyFlow/1.4"
   "Unknown new useragent in the wild/v0.1.0"])


(defn -main
  [& _args]
  (println "=== uap-clj benchmarks ===\n")

  (println "--- browser (single UA) ---")
  (crit/bench (browser (first sample-uas)))

  (println "\n--- os (single UA) ---")
  (crit/bench (os (first sample-uas)))

  (println "\n--- device (single UA) ---")
  (crit/bench (device (first sample-uas)))

  (println "\n--- useragent (single UA, all fields) ---")
  (crit/bench (useragent (first sample-uas)))

  (println "\n--- useragent (batch of 5 UAs) ---")
  (crit/bench (mapv useragent sample-uas))

  (println "\n--- browser (unknown UA) ---")
  (crit/bench (browser (last sample-uas))))
