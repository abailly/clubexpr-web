(ns club.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)

; Layer 1

(rf/reg-sub
 :current-page
 (fn [db]
   (:current-page db)))

(rf/reg-sub
 :attempt-code
 (fn [db]
   (:attempt-code db)))

(rf/reg-sub
 :profile-quality
 (fn [db]
   (-> db :profile-page :quality)))

(rf/reg-sub
 :profile-school
 (fn [db]
   (-> db :profile-page :school)))

(rf/reg-sub
 :profile-lastname
 (fn [db]
   (-> db :profile-page :lastname)))

(rf/reg-sub
 :profile-firstname
 (fn [db]
   (-> db :profile-page :firstname)))

; Layer 2

(rf/reg-sub
  :help-text-find-you
  (fn [query-v _]
     (rf/subscribe [:profile-quality]))
  (fn [profile-quality query-v _]
    (case profile-quality
      "scholar" (t ["pour que votre professeur puisse vous retrouver"])
      "teacher" (t ["pour que les élèves puissent vous retrouver"])
      (t ["pour que l’on puisse vous retrouver"]))))

(rf/reg-sub
 :profile-school-pretty
  (fn [query-v _]
     (rf/subscribe [:profile-school]))
  (fn [profile-school query-v _]
    (case profile-school
      "fake-id-no-school" (t ["Aucun établissement"])
      (->> (club.db/get-schools!)
           (filter #(= profile-school (:id %)))
           first
           :name))))
