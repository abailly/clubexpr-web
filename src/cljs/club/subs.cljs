(ns club.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :current-page
 (fn [db]
   (:current-page db)))

(rf/reg-sub
 :attempt-code
 (fn [db]
   (:attempt-code db)))
