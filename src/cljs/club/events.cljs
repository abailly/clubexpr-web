(ns club.events
  (:require
    [re-frame.core :as re-frame]
    [club.db]
    [club.utils :refer [parse-url]]
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

(re-frame/reg-event-db
  :nav
  (fn [db _]
    (if (empty? db) db  ; do not alter app-db on loading the page
      (let [parsed-url (parse-url (-> js/window .-location .-href))
            page (:page parsed-url)]
            (assoc db :current-page page)))))
