(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [club.utils :refer [FormControlFixed]]
            [club.config :as config]
            [cljs.pprint :refer [pprint]]))

; Placeholder for future translation mechanism
(defn t [[txt]] txt)

(defn bs
  ([component]
   (getValueByKeys js/window "deps" "react-bootstrap" (str component)))
  ([c subc]
   (getValueByKeys js/window "deps" "react-bootstrap" (str c) (str subc))))

; Profile components
(defn profile-input [{:keys [label placeholder help value-id event-id]}]
  [:> (bs 'FormGroup) {:controlId "formBasicText"
                       :validationState nil}
    [:> (bs 'ControlLabel) label]
    [FormControlFixed {:type "text"
                       :value @(rf/subscribe [value-id])
                       :placeholder placeholder
                       :on-change #(rf/dispatch [event-id
                                                 (-> % .-target .-value)])}]
    [:> (bs 'FormControl 'Feedback)]
    [:> (bs 'HelpBlock) help]])

(defn src-input
  []
  [:form {:role "form"}
    [:> (bs 'FormGroup) {:controlId "formBasicText"
                         :validationState nil}
      [:> (bs 'ControlLabel) (t ["Code Club: "])]
      [FormControlFixed {:type "text"
                         :value @(rf/subscribe [:attempt-code])
                         :placeholder "(Somme 1 2)"
                         :on-change #(rf/dispatch
                                       [:user-code-club-src-change
                                        (-> % .-target .-value)])}]
      [:> (bs 'FormControl 'Feedback)]
      [:> (bs 'HelpBlock) (t ["Taper du Code Club"])]]])

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
            [:a {} (t ["Profil"])]]]
        [:> (bs 'Nav) {:pullRight true}
          [:> (bs 'NavItem) {:eventKey 1
                             :href "#access_token=vz5yciAapGMaZj2f&expires_in=86400&id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1qZ3dOVFZGUkVRMU9FTkNNVEV6UWpreVJVVkVRa1EwUmpGRlJVVkZPVVZGUXpjelJFWTJSQSJ9.eyJpc3MiOiJodHRwczovL2NsdWJleHByLmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEwNDY3NjMzMjUwNjQ4NDkwOTAzNSIsImF1ZCI6IlFLcTQ4ak5acVE4NFZRQUxTWkVBQmt1VW5NNzR1SFVhIiwiZXhwIjoxNTAyNTU5NDM4LCJpYXQiOjE1MDI1MjM0MzgsIm5vbmNlIjoiUGFHUn5GcC02a2F1NnV4RFI4TU8xeW1hNUp1OTJJUi0iLCJhdF9oYXNoIjoickNDQ09oN2w0Q2NmbFM3MXRGRHR0USJ9.obNRi9dlaL6mB-06OHyr9As5Ny-2PPC6QZGNPm0nLE0nv174VaUK3NhLpVKxvBAtggIcelK0IqVf7v4Wm6tOoCl4FcO8tUKCbz3oWqY5_pBAoWmW2mOVIJeEa9nMixMZdJgZ0U_F1d-jxC4ueLRTTNom0eY8e4gHICcfeh50al3qCjgCIQnvxWrKXDZOuBAM3r7pk5ofHawEWxI-2U9UxVlJwMeQER2yceb_i7zJ2d2SCyeeHc5SA3XogFpYYtSrSudBfT9h-53cK56xhvRksmf9kS0YATqVB0_ZYiC8vqDnmbBnWpbGuneSQm4HZWi22uKIQ1SX7Snnr_mfUvjYww&token_type=Bearer&state=TAaUZxkG6LpdcyTaRScVk8SjUrxiKRMa"}
            (t ["Direct Token"])]
          [:> (bs 'NavItem) {:eventKey 1
                             :on-click #(rf/dispatch [:login])
                             :href "#/"}
            (t ["Login"])]]]]
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
  (let [lastname  [profile-input {:label (t ["Nom"])
                                  :placeholder (t ["Klougliblouk"])
                                  :help (str (t ["Votre nom de famille"])
                                             " "
                                             @(rf/subscribe [:help-text-find-you]))
                                  :value-id :profile-lastname
                                  :event-id :profile-lastname}]
        firstname [profile-input {:label (t ["Prénom"])
                                  :placeholder (t ["Georgette"])
                                  :help (str (t ["Votre prénom"])
                                             " "
                                             @(rf/subscribe [:help-text-find-you]))
                                  :value-id :profile-firstname
                                  :event-id :profile-firstname}]]
    [:div
      [:div.jumbotron
        [:h2 (t ["Votre profil"])]]
      [:form {:role "form"}
        [:div {:style {:margin-bottom "1em"}}  ; TODO CSS
          [:> (bs 'ButtonToolbar)
            [:> (bs 'ToggleButtonGroup)
                {:type "radio"
                 :name "quality"
                 :defaultValue "scholar"
                 :on-change #(rf/dispatch [:profile-quality %])}
              [:> (bs 'ToggleButton) {:value "scholar"} (t ["Élève"])]
              [:> (bs 'ToggleButton) {:value "teacher"} (t ["Professeur"])]]]]
        lastname
        (if (= "scholar" @(rf/subscribe [:profile-quality]))
          firstname
          "")
      ]
    ]))

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
