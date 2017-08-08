(ns club.db
  (:require [cljs.spec :as s]))

(s/def ::current-page keyword?)
(s/def ::attempt-code string?)
(s/def ::db (s/keys :req-un [::current-page ::attempt-code]))

(def default-db
  {:current-page :landing
   :attempt-code "(Somme 2 2)"})
