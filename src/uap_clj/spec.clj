(ns uap-clj.spec
  "clojure.spec definitions for uap-clj parsed output"
  (:require
   [clojure.spec.alpha :as s]))


;; Shared field specs: version components are strings or nil
(s/def ::family (s/nilable string?))
(s/def ::major (s/nilable string?))
(s/def ::minor (s/nilable string?))
(s/def ::patch (s/nilable string?))


;; Browser result
(s/def ::browser
  (s/keys :req-un [::family ::major ::minor ::patch]))


;; OS result
(s/def ::patch_minor (s/nilable string?))


(s/def ::os
  (s/keys :req-un [::family ::major ::minor ::patch ::patch_minor]))


;; Device result
(s/def ::brand (s/nilable string?))
(s/def ::model (s/nilable string?))


(s/def ::device
  (s/keys :req-un [::family ::brand ::model]))


;; Full useragent result
(s/def ::ua (s/nilable string?))


(s/def ::useragent
  (s/keys :req-un [::ua ::browser ::os ::device]))
