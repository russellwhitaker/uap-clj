(ns uap-clj.device
  "Useragent device lookup"
  (:require [uap-clj.common :refer [regexes-all first-match field]]
            [clojure.java.io :as io :refer [resource]]
            [clojure.string :as s :refer [join trim]]))

(def regexes (:device_parsers regexes-all))

(defn device
  [ua]
  (try
    (let [match (first-match ua regexes)
          result (first (flatten (vector (:result match))))]
      (if (= "Other" result)
        {:family "Other" :brand nil :model nil}
        (let [family (field match :device_replacement 1)
              brand (field match :brand_replacement 2)
              model (field match :model_replacement 1)]
          {:family family :brand brand :model model})))
  (catch java.lang.IndexOutOfBoundsException e
    {:family "Other" :brand nil :model nil})))

; For use in production settings where speed may be preferred
;  in exchange for the tradeoff of increased memory bloat:
(def device-memoized (memoize device))
