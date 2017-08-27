(ns club.subs
  (:require [re-frame.core :as rf]
            [reagent.ratom :refer [make-reaction]]
            [clojure.walk :refer [keywordize-keys]]
            [club.utils :refer [data-from-js-obj]]
            [club.db :refer [get-users!]]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)

; Layer 1

(rf/reg-sub
 :current-page
 (fn [db]
   (:current-page db)))

(rf/reg-sub
 :authenticated
 (fn [db]
   (:authenticated db)))

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
 :profile-teacher
 (fn [db]
   (-> db :profile-page :teacher)))

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

(rf/reg-sub-raw
 :profile-teachers-list
  (fn [app-db _]
    (let [school-id (get-in @app-db [:profile-page :school])
          _ (get-users!
              {:on-success
                #(rf/dispatch
                  [:write-teachers-list
                    (->> % data-from-js-obj
                           (filter (fn [x] (= "teacher" (:quality x))))
                           (filter (fn [x] (= school-id (:school x))))
                           (map (fn [x] {:id (:id x) :lastname (:lastname x)}))
                           vec)])})]
      (make-reaction
        (fn [] (get-in @app-db [:profile-page :teachers-list] []))
        :on-dispose #(do)))))

(rf/reg-sub
 :profile-teacher-pretty
  (fn [query-v _]
    [(rf/subscribe [:profile-teacher])
     (rf/subscribe [:profile-teachers-list])])
  (fn [[profile-teacher profile-teachers-list] query-v _]
    (case profile-teacher
      "no-teacher" (t ["Pas de professeur"])
      (->> profile-teachers-list
           (filter #(= profile-teacher (:id %)))
           first
           :lastname))))

(rf/reg-sub
 :groups-selected
 (fn [db]
   ["gr1" "gr2"]))
