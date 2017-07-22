(ns club.db
  (:require [cljs.spec :as s]))

(s/def ::attempt-code string?)
(s/def ::db (s/keys :req-un [::attempt-code]))

(def default-db
  {:attempt-code "(Somme 2 2)"})
