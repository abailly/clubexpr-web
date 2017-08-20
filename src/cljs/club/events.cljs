(ns club.events
  (:require
    [clojure.string :as str]
    [re-frame.core :as rf]
    [re-frame.db :refer [app-db]]
    [goog.object :refer [getValueByKeys]]
    [club.db]
    [club.utils :refer [parse-url get-url-all! get-url-root!]]
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

(rf/reg-event-db
  :logout
  (fn [db _]
    (assoc db :current-page :landing
              :authenticated false
              :auth-data {:access-token ""
                          :expires-at   ""
                          :user-id      ""})))

(rf/reg-event-fx
  :profile-cancel
  (fn []
    {:profile-cancel nil}))

(rf/reg-fx
  :profile-cancel
  (fn [_]
    (club.db/fetch-profile-data!)
    ))

(rf/reg-event-fx
  :profile-save
  (fn []
    {:profile-save nil}))

(rf/reg-fx
  :profile-save
  (fn [_]
    (println "save")
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
                 {:db new-db :auth query-params})]
       cofx)))

(rf/reg-fx
  :auth
  (fn [{:keys [access_token expires_in id_token]}]  ; we left: token_type state
    ; Clean the URL
    ; window.location.hash = '';

    ; Store auth time
    ; TODO

    (let [expires-in (js/parseInt expires_in)
          expires-at (str (+ (* expires-in 1000) (.getTime (new js/Date))))
          decoded-json (-> id_token
                          (str/split ".")
                          second
                          decodeString)
          decoded-js (try (.parse js/JSON decoded-json)
                          (catch js/Object e (println e)
                                             (println id_token)
                                             (println decoded-json)
                                             js/Object))
          user-id (getValueByKeys decoded-js "sub")]
      (if (not (nil? user-id))
        (do
          (swap! app-db assoc-in [:authenticated] true)
          (swap! app-db assoc-in [:auth-data :access-token] access_token)
          (swap! app-db assoc-in [:auth-data :expires-at] expires-at)
          (swap! app-db assoc-in [:auth-data :user-id] user-id)))
          (check-and-throw :club.db/db @app-db)
    )))
