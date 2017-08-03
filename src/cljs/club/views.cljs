(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [club.config :as config]
            [cljs.pprint :refer [pprint]]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)

(defn src-input
  []
  [:div
   (t ["Code Club: "])
   [:input {:type "text"
            :value @(rf/subscribe [:attempt-code])
            :on-change #(rf/dispatch [:user-code-club-src-change
                                      (-> % .-target .-value)])}]])

(defn rendition
  [src]
  (let [react-mathjax (getValueByKeys js/window "deps" "react-mathjax")
        ctx (getValueByKeys react-mathjax "Context")
        node (getValueByKeys react-mathjax "Node")
        clubexpr (getValueByKeys js/window "deps" "clubexpr")
        renderLispAsLaTeX (.-renderLispAsLaTeX clubexpr)]
    [:> ctx [:> node {:inline true} (renderLispAsLaTeX src)]]))
 
(defn main-panel []
  (fn []
    [:div
      [:h1 (t ["Club des Expressions"])]
      (when config/debug? [:pre (with-out-str (pprint @app-db))])
      [:p (t ["Bonjour, veuillez taper du Code Club ci-dessous."])]
      [src-input]
      [rendition @(rf/subscribe [:attempt-code])]]))
