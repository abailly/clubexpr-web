(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [club.config :as config]
            [cljs.pprint :refer [pprint]]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)


(defn bs
  ([component]
   (getValueByKeys js/window "deps" "react-bootstrap" (str component)))
  ([c subc]
   (getValueByKeys js/window "deps" "react-bootstrap" (str c) (str subc))))

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
    [:div {:style {:min-height "2em"}}
      [:> ctx [:> node (renderLispAsLaTeX src)]]]))
 
(defn nav-bar
  []
  (let [page @(rf/subscribe [:current-page])
        active #(if (= %1 %2) "active" "")]
    [:> (bs 'Navbar)
      [:> (bs 'Navbar 'Header)
        [:> (bs 'Navbar 'Brand)
          [:a {:href "/"} (t ["Club des Expressions"])]]
        [:> (bs 'Navbar 'Toggle)]]
      [:> (bs 'Navbar 'Collapse)
        [:> (bs 'Nav)
          [:> (bs 'NavItem) {:eventKey 1
                             :href "#/"
                             :class (active page :landing)}
            [:a {} (t ["Accueil"])]]
          [:> (bs 'NavItem) {:eventKey 2
                             :href "#/profile"
                             :class (active page :profile)}
            [:a {} (t ["Profil"])]]]]]
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
    [:> (bs 'Grid)
      [:> (bs 'Row)
        [:h1 (t ["Qu’est-ce que le Club des Expressions ?"])]
        [:> (bs 'Col) {:xs 6 :md 6}
          [:h2 (t ["Pour les enseignants"])]
          [:p (t ["Le Club des Expressions vous permet de faire travailler vos élèves sur le sens et la structure des expressions mathématiques."])]
          [:p (t ["Créez votre compte, faites créer un compte à vos élèves, et vous pourrez leur attribuer des séries d’expressions à reconstituer."])]]
        [:> (bs 'Col) {:xs 6 :md 6}
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
