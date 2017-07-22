(ns club.events
  (:require
    [re-frame.core :as re-frame]
    [club.db]
    [re-frame.core :refer [reg-event-db
                           after debug]]
    [cljs.spec     :as s]))


;; Interceptors

(defn check-and-throw
  "Throw an exception if db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :club.db/db)))


;; Event Handlers

(re-frame/reg-event-db
  :user-code-club-src-change
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc db :attempt-code new-value)))

(re-frame/reg-event-db
  :initialize-db
  [check-spec-interceptor]
  (fn  [_ _]
    club.db/default-db))
