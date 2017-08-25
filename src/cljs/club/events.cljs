(ns club.events
  (:require
    [clojure.string :as str]
    [clojure.walk :refer [keywordize-keys]]
    [re-frame.core :as rf]
    [re-frame.db :refer [app-db]]
    [goog.object :refer [getValueByKeys]]
    [club.db]
    [club.db :refer [base-user-record set-auth-data!]]
    [club.utils :refer [data-from-js-obj parse-url get-url-all! get-url-root!]]
    [cljs.spec     :as s]
    [goog.crypt.base64 :refer [decodeString]]))


;; Interceptors

(defn check-and-throw
  "Throw an exception if db doesn't match the spec"
  [a-spec db]
  (let [valid (s/valid? a-spec db)]
  (when-not valid
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {})))
  valid))

(def check-spec-interceptor (rf/after (partial check-and-throw :club.db/db)))


;; Event Handlers

(rf/reg-event-fx
  :login
  (fn []
    {:login nil}))

(def webauth
  (let [auth0 (getValueByKeys js/window "deps" "auth0")
        opts (clj->js {:domain "clubexpr.eu.auth0.com"
                       :clientID "QKq48jNZqQ84VQALSZEABkuUnM74uHUa"
                       :responseType "token id_token"
                       :redirectUri (get-url-root!)
                       })]
    (new auth0.WebAuth opts)))

(rf/reg-fx
  :login
  (fn [_]
    (.authorize webauth)))

(rf/reg-event-fx
  :logout
  (fn [{:keys [db]} _]
    {:db (assoc db :current-page :landing
                   :authenticated false
                   :auth-data {:kinto-id ""
                               :auth0-id ""
                               :access-token ""
                               :expires-at   ""}
                   :profile-page {:quality "scholar"
                                  :school "fake-id-no-school"
                                  :teacher "no-teacher"
                                  :teachers-list ""
                                  :lastname ""
                                  :firstname ""})
     :clean-url nil}))

(rf/reg-fx
  :clean-url
  (fn []
    (set! (-> js/window .-location .-hash) "")))

(rf/reg-event-fx
  :profile-cancel
  (fn []
    {:profile-cancel nil}))

(rf/reg-fx
  :profile-cancel
  (fn [_]
    (club.db/fetch-profile-data!)
    ; TODO useless use of set-auth-data! : these 4 already set
    ;(swap! app-db assoc-in [:authenticated] true)
    ;; from new-user-data
    ;(swap! app-db assoc-in [:auth-data :auth0-id] auth0-id)
    ;(swap! app-db assoc-in [:auth-data :access-token] access-token)
    ;(swap! app-db assoc-in [:auth-data :expires-at] expires-at)
    ))

(rf/reg-event-fx
  :profile-save
  (fn []
    {:profile-save nil}))

(rf/reg-fx
  :profile-save
  (fn [_]
    (.. club.db/k-users
        (updateRecord (clj->js
                        {:id       (-> @app-db :auth-data :kinto-id)
                         :auth0-id (-> @app-db :auth-data :auth0-id)
                         :quality   (-> @app-db :profile-page :quality)
                         :school    (-> @app-db :profile-page :school)
                         :teacher   (-> @app-db :profile-page :teacher)
                         :lastname  (-> @app-db :profile-page :lastname)
                         :firstname (-> @app-db :profile-page :firstname)}))
        (then #(rf/dispatch [:profile-save-ok]))
    )))

(rf/reg-event-db
  :profile-save-ok
  [check-spec-interceptor]
  (fn [db [_ _]]
    ; TODO: set a flag in the state to display «new profile saved»
    db
    ))

(rf/reg-event-db
  :user-code-club-src-change
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc db :attempt-code new-value)))

(rf/reg-event-db
  :profile-quality
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :quality] new-value)))

(rf/reg-event-db
  :profile-school
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :school] new-value)))

(rf/reg-event-db
  :profile-teacher
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :teacher] new-value)))

(rf/reg-event-db
  :profile-lastname
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :lastname] new-value)))

(rf/reg-event-db
  :profile-firstname
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :firstname] new-value)))

(rf/reg-event-db
  :initialize-db
  [check-spec-interceptor]
  (fn  [_ _]
    club.db/default-db))

(rf/reg-event-fx
  :nav
  (fn [{:keys [db]} _]
    (let [parsed-url (parse-url (get-url-all!))
          page (:page parsed-url)
          query-params (:query-params parsed-url)
          ; 'empty?' prevents wrecking the state at loading time
          new-db (if (empty? db) db (assoc db :current-page page))
          cofx (if (empty? query-params)
                 {:db new-db}
                 {:db new-db :auth query-params :clean-url nil})]
       cofx)))

(defn process-user-check!
  [result new-user-data]
  (let [new-auth0-id (:auth0-id new-user-data)
        user-with-same-auth0-id (->> result
                                     data-from-js-obj
                                     (filter #(= new-auth0-id (:auth0-id %)))
                                     first)]
    (if (nil? user-with-same-auth0-id)
      (.. club.db/k-users
          (createRecord (clj->js (base-user-record new-auth0-id)))
          (then #(set-auth-data! (merge new-user-data (data-from-js-obj %)))))
      (set-auth-data! (merge new-user-data user-with-same-auth0-id)))))

(rf/reg-fx
  :auth
  (fn [{:keys [access_token expires_in id_token]}]  ; we left: token_type state
    (let [expires-in-num (js/parseInt expires_in)
          expires-at (str (+ (* expires-in-num 1000) (.getTime (new js/Date))))
          decoded-json (-> id_token
                          (str/split ".")
                          second
                          decodeString)
          decoded-js (try (.parse js/JSON decoded-json)
                          (catch js/Object e (println e)
                                             (println id_token)
                                             (println decoded-json)
                                             js/Object)) ; default: empty obj
          auth0-id (getValueByKeys decoded-js "sub")
          new-user-data {:auth0-id auth0-id
                         :access-token access_token
                         :expires-at expires-at}]
      (if (not (nil? auth0-id))
        (.. club.db/k-users
            (listRecords)
            (then #(process-user-check! % new-user-data))
            (catch #(js/alert %))))
    )))

(rf/reg-event-db
  :write-teachers-list
  [check-spec-interceptor]
  (fn [db [_ new-value]]
    (assoc-in db [:profile-page :teachers-list] new-value)))
