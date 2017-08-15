(ns club.db
  (:require [cljs.spec :as s]))

(s/def ::current-page keyword?)
(s/def ::attempt-code string?)

(s/def ::profile-page
  (s/and ;map?
         (s/keys :req-un [::quality ::lastname ::firstname])))
(s/def ::quality string?)
(s/def ::school string?)
(s/def ::lastname string?)
(s/def ::firstname string?)

(s/def ::db (s/keys :req-un [::current-page
                             ::attempt-code
                             ::profile-page
                             ]))

(def default-db
  {:current-page :landing
   :attempt-code "(Somme 1 (Produit 2 x))"
   :profile-page {:quality "scholar"
                  :school "fake-id-no-school"
                  :lastname ""
                  :firstname ""}})
