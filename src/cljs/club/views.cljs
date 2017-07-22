(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [webpack.bundle]
            [club.config :as config]
            [cljs.pprint :refer [pprint]]))

(defn src-input
  []
  [:div
   "Code Club: "
   [:input {:type "text"
            :value @(rf/subscribe [:attempt-code])
            :on-change #(rf/dispatch [:user-code-club-src-change
                                      (-> % .-target .-value)])}]])

(defn rendition
  [src]
  (let [react-mathjax (aget js/window "deps" "react-mathjax")
        ctx (aget react-mathjax "Context")
        node (aget react-mathjax "Node")]
    [:div
      [:> ctx [:> node {:inline true} src]]]))

(defn main-panel []
  (let [name @(rf/subscribe [:attempt-code])]
    (fn []
      [:div
        [:h1 "Club des Expressions"]
        (when config/debug? [:pre (with-out-str (pprint @app-db))])
        [:p "Bonjour, veuillez taper du Code Club ci-dessous."]
        [src-input]
        [rendition @(rf/subscribe [:attempt-code])]])))
