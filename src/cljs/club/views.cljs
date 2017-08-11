(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [club.config :as config]
            [cljs.pprint :refer [pprint]]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)

(def react-bootstrap (getValueByKeys js/window "deps" "react-bootstrap"))
(def Grid (getValueByKeys react-bootstrap "Grid"))
(def Row  (getValueByKeys react-bootstrap "Row"))
(def Col  (getValueByKeys react-bootstrap "Col"))

(defn src-input
  []
  [:form {:role "form"}
    [:div.form-group
      [:label {:for "codeclub"} (t ["Code Club: "])]
        [:input#codeclub {:type "text"
                 :class "form-control"
                 :value @(rf/subscribe [:attempt-code])
                 :on-change #(rf/dispatch [:user-code-club-src-change
                                          (-> % .-target .-value)])}]]])

(defn rendition
  [src]
  (let [react-mathjax (getValueByKeys js/window "deps" "react-mathjax")
        ctx (getValueByKeys react-mathjax "Context")
        node (getValueByKeys react-mathjax "Node")
        clubexpr (getValueByKeys js/window "deps" "clubexpr")
        renderLispAsLaTeX (.-renderLispAsLaTeX clubexpr)]
    [:> ctx [:> node (renderLispAsLaTeX src)]]))
 
(defn nav-bar
  []
  (let [page @(rf/subscribe [:current-page])
        active #(if (= %1 %2) "active" "")]
    [:nav.navbar.navbar-default
      [:div.container
        [:div.navbar-header
          [:button.navbar-toggle {:type "button"
                                  :data-toggle "collapse"
                                  :data-target "#navbar"
                                  :aria-expanded "false"
                                  :aria-controls "navbar"}
            [:span.sr-only "Toggle navigation"]
            [:span.icon-bar]
            [:span.icon-bar]
            [:span.icon-bar]]
          [:a.navbar-brand {:href "#/"} (t ["Club des Expressions"])]]
        [:div#navbar.navbar-collapse
          [:ul.nav.navbar-nav
           [:li {:class (active page :landing)}
             [:a {:href "#/"} (t ["Accueil"])]]
           [:li {:class (active page :profile)}
             [:a {:href "#/profile"} (t ["Profil"])]]
          ]]]]
     ))

(defn page-landing
  []
  [:div
    [:div.jumbotron
      [:h2 (t ["Nouveau venu ?"])]
      [:p (t ["Bonjour, tapez du Code Club ci-dessous pour former une expression mathématique."])]
      [:p (t ["Parmi les commandes disponibles, il y a :"])
          [:code "Somme"] ", "
          [:code "Diff"] ", "
          [:code "Produit"] ", "
          [:code "Produit"] ", "
          [:code "Carre"] ", "
          [:code "Racine"] "."]
      [src-input]
      [rendition @(rf/subscribe [:attempt-code])]]
    [:> Grid
      [:> Row
        [:h1 (t ["Qu’est-ce que le Club des Expressions ?"])]
        [:> Col {:xs 6 :md 6}
          [:h2 (t ["Pour les enseignants"])]
          [:p (t ["Le Club des Expressions vous permet de faire travailler vos élèves sur le sens et la structure des expressions mathématiques."])]
          [:p (t ["Créez votre compte, faites créer un compte à vos élèves, et vous pourrez leur attribuer des séries d’expressions à reconstituer."])]]
        [:> Col {:xs 6 :md 6}
          [:h2 (t ["Pour les élèves"])]
          [:p (t ["Le Club des Expressions vous permet de travailler sur le sens et la structure des expressions mathématiques."])]
          [:p (t ["Si votre professeur n’utilise pas le Club, vous pourrez quand même obtenir des séries d’expressions à reconstituer. Pour cela, créez votre compte."])]
          [:p (t ["Il est préférable bien sûr que votre professeur vous guide, mettez cette personne au courant !"])]]
      ]]])

(defn page-profile
  []
  [:div
    [:div.jumbotron
      [:h2 (t ["Votre profil"])]
    ]
  ])

(defn main-panel []
  (fn []
    [:div.container
      [nav-bar]
      (case @(rf/subscribe [:current-page])
        :profile [page-profile]
        :landing [page-landing])
      (when (and false config/debug?) [:pre {:style {:bottom "0px"}}
                                           (with-out-str (pprint @app-db))])
    ]
    ))
