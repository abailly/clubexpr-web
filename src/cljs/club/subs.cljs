(ns club.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :current-page
 (fn [db]
   (:current-page db)))

(re-frame/reg-sub
 :attempt-code
 (fn [db]
   (:attempt-code db)))
