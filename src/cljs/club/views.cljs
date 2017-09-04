(ns club.views
  (:require [re-frame.core :as rf]
            [re-frame.db :refer [app-db]]
            [goog.object :refer [getValueByKeys]]
            [webpack.bundle]
            [club.utils :refer [groups-option scholar-comparator FormControlFixed]]
            [club.config :as config]
            [club.db]
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
  [{:keys [label help]}]
  [:form {:role "form"}
    [:> (bs 'FormGroup) {:controlId "formBasicText"
                         :validationState nil}
      [:> (bs 'ControlLabel) label]
      [FormControlFixed {:type "text"
                         :value @(rf/subscribe [:attempt-code])
                         :placeholder "(Somme 1 2)"
                         :on-change #(rf/dispatch
                                       [:user-code-club-src-change
                                        (-> % .-target .-value)])}]
      [:> (bs 'FormControl 'Feedback)]
      [:> (bs 'HelpBlock) help]]])

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
        active #(if (= %1 %2) "active" "")
        authenticated @(rf/subscribe [:authenticated])
        quality @(rf/subscribe [:profile-quality])]
    [:> (bs 'Navbar)
      [:div.container-fluid
        [:> (bs 'Navbar 'Header)
          [:> (bs 'Navbar 'Brand) (t ["Club des Expressions"])]
          [:> (bs 'Navbar 'Toggle)]]
        [:> (bs 'Navbar 'Collapse)
          [:> (bs 'Nav)
            [:> (bs 'NavItem) {:eventKey 1
                               :href "#/"
                               :class (active page :landing)} (t ["Accueil"])]
            [:> (bs 'NavItem) {:eventKey 1
                               :href "#/help"
                               :class (active page :help)} (t ["Aide"])]
            (if authenticated
              [:> (bs 'NavItem) {:eventKey 2
                                 :href "#/profile"
                                 :class (active page :profile)} (t ["Profil"])])
            (if (= quality "teacher")
              [:> (bs 'NavItem) {:eventKey 3
                                 :href "#/groups"
                                 :class (active page :groups)} (t ["Groupes"])])]
          [:> (bs 'Nav) {:pullRight true}
            (if authenticated
              [:> (bs 'NavItem)
                  {:eventKey 1 :on-click #(rf/dispatch [:logout])} (t ["Déconnexion"])]
              [:> (bs 'NavItem)
                  {:eventKey 1 :on-click #(rf/dispatch [:login])}  (t ["Connexion"])])
          ]]]]
     ))

(defn footer
  []
  [:div.container.small {:style {:color "#aaa"}}  ; TODO CSS
    [:br]
    [:br]
    [:br]
    [:hr]
    [:> (bs 'Grid)
      [:> (bs 'Row)
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Contact"])]
          [:ul
            [:li "Twitter : "
              [:a {:href "https://twitter"} "@ClubExpr"]
              " (" (t ["Publication d’une expression intéressante par semaine !"]) ")"]
            [:li "Email : "
              [:a {:href "mailto:profgraorg.org@gmail.com"} "profgra@gmail.com"]]
            [:li "Github : "
              [:a {:href "https://github.com/ClubExpressions/clubexpr-web/"}
                  "ClubExpressions/clubexpr"]]]]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Statut"])]
          [:p "Le Club des Expressions est en constante évolution. N’hésitez pas à signaler des bugs ou nous faire part de suggestions."]]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Remerciements"])]
          [:p (t ["Réalisé avec l’aide, aimable autant que redoutable, de :"])]
          [:ul
            [:li "Jean-Philippe Rouquès (aide pédagogique)"]
            [:li "Damien Lecan (aide technique)"]
            [:li "tous les collègues et élèves sympathisants (aide moral)"]
            [:li "tous les logiciels sur lesquels est bâti le Club (épaules de géants)"]]]
       ]]])

(defn page-landing
  []
  [:div
    [:div.jumbotron
      [:h2 (t ["Nouveau venu ?"])]
      (let [label (t ["Tapez du Code Club ci-dessous pour former une expression mathématique."])
            help [:span (t ["Commandes disponibles :"])
                   [:code "Somme"] ", "
                   [:code "Diff"] ", "
                   [:code "Produit"] ", "
                   [:code "Quotient"] ", "
                   [:code "Oppose"] ", "
                   [:code "Inverse"] ", "
                   [:code "Carre"] ", "
                   [:code "Puissance"] " et "
                   [:code "Racine"] "."]]
        [src-input {:label label :help help}])
      [:br]
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
          [:p (t ["Il est préférable bien sûr que votre professeur vous guide, mettez cette personne au courant !"])]]]]
  ])

(defn page-help-guest
  []
  [:div
    [:div.jumbotron
      [:h2 (t ["Nous ne pouvons pas encore vous donner de l’aide."])]]
    [:h3 (t ["Commencez par vous connecter (bouton en haut à droite)."])]
    [:p (t ["Une aide vous sera proposée en fonction de votre profil."])]
    [:p (t ["En cas de problème pour vous connecter, veuillez nous contacter :"])]
    [:ul
      [:li "par email : "
        [:a {:href "mailto:profgraorg.org@gmail.com"} "profgra@gmail.com"]]
      [:li "sur Github : "
        [:a {:href "https://github.com/ClubExpressions/clubexpr-web/"}
            "ClubExpressions/clubexpr"]]
      [:li "via Twitter : "
        [:a {:href "https://twitter"} "@ClubExpr"]]]
   ])

(defn empty-profile
  []
  [:div
    [:h2 (t ["Votre profil semble vide"])]
    [:p (t ["Vous n’avez pas indiqué votre nom. Veuillez remplir votre profil et revenir sur cette page."])]])

(defn page-help-scholar
  []
  [:div
    [:div.jumbotron
      (if (empty? @(rf/subscribe [:profile-lastname]))
        [empty-profile]
        [:div
          [:h2 (t ["Aide pour les élèves"])]
          [:p (t ["Si vous n’êtes pas élève, modifiez votre profil. Vous pourrez y indiquer votre qualité de professeur.."])]])]
    [:> (bs 'Grid)
      [:> (bs 'Row)
        [:h1 (t ["Qu’est-ce que le Club des Expressions ?"])]
        [:> (bs 'Col) {:xs 6 :md 6}
          [:h2 (t ["Pour les élèves"])]
          [:p (t ["Le Club des Expressions vous permet de travailler sur le sens et la structure des expressions mathématiques."])]
          [:p (t ["Si votre professeur n’utilise pas le Club, vous pourrez quand même obtenir des séries d’expressions à reconstituer. Il est préférable bien sûr que votre professeur vous guide, mettez cette personne au courant !"])]
        ]
        [:> (bs 'Col) {:xs 6 :md 6}
          [:h2 (t ["Pour les enseignants"])]
          [:p (t ["Le Club des Expressions permet aux enseignants de faire travailler leurs élèves sur le sens et la structure des expressions mathématiques."])]
        ]
      ]
      [:> (bs 'Row)
        [:h1 (t ["Ce que l’on peut faire au Club"])]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Définir son profil"])]
          [:p (t ["Dans la partie « Profil », déclarez votre établissement puis votre professeur (si vous n’en avez pas, choisissez « Pas de professeur »)."])]
          [:p (t ["Grâce à votre nom et prénom, votre professeur pourra vous inclure dans un ou plusieurs groupes de travail."])]
        ]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Faire le travail donné"])]
          [:p (t ["Dans la partie « Travail », vous trouverez le travail que votre professeur vous propose de faire."])]
        ]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["S’entraîner"])]
          [:p (t ["Le Club des Expressions vous proposera parfois, dans la partie « Travail », des séries à faire pour vous entraîner."])]
        ]
      ]
    ]])

(defn page-help-teacher
  []
  [:div
    [:div.jumbotron
      (if (empty? @(rf/subscribe [:profile-lastname]))
        [empty-profile]
        [:div
          [:h2 (t ["Aide pour les professeurs"])]
          [:p (t ["Si vous n’êtes pas professeur, modifiez votre profil sinon votre professeur ne pourra pas vous retrouver."])]])]
    [:> (bs 'Grid)
      [:> (bs 'Row)
        [:h1 (t ["Qu’est-ce que le Club des Expressions ?"])]
        [:> (bs 'Col) {:xs 6 :md 6}
          [:h2 (t ["Pour les enseignants"])]
          [:p (t ["Le Club des Expressions vous permet de faire travailler vos élèves sur le sens et la structure des expressions mathématiques."])]]
        [:> (bs 'Col) {:xs 6 :md 6}
          [:h2 (t ["Pour les élèves"])]
          [:p (t ["Le Club des Expressions permet aux élèves de travailler sur le sens et la structure des expressions mathématiques."])]
          [:p (t ["Si leur professeur n’utilise pas le Club, les élèves peuvent quand même obtenir des séries d’expressions à reconstituer grâce à des professeurs-robots."])]
          [:p (t ["Il est préférable bien sûr que leur professeur les guide !"])]]]
      [:> (bs 'Row)
        [:h1 (t ["Ce que l’on peut faire au Club"])]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Définir son profil"])]
          [:p (t ["Dans la partie « Profil », déclarez votre établissement puis votre professeur."])]
          [:p (t ["Grâce à votre nom, vos élèves pourront vous choisir comme « professeur référent » et apparaîtront dans votre partie « Groupes »."])]
        ]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Regrouper ses élèves"])]
          [:p (t ["Dans la partie « Groupes », vous définirez des listes d’élèves. Ces listes peuvent correspondre :"])]
          [:ul
            [:li (t ["à des classes entières ;"])]
            [:li (t ["à des demis-groupes d’une classe ;"])]
            [:li (t ["à des élèves ayant des besoins spécifiques (remédiation ou approfondissement) au sein de l’Accompagnement Personnalisé ou non ;"])]
            [:li (t ["…"])]]
        ]
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Assigner des séries à ses groupes"])]
          [:p (t ["Une fois que vous aurez créé une série dans la partie « Séries », vous pourrez l’attribuer à un groupe. Cette attribution se fait dans la partie « Groupes »."])]
        ]
      ]
      [:> (bs 'Row)
        [:> (bs 'Col) {:xs 4 :md 4}
          [:h2 (t ["Ce que voit un élève"])]
          [:p (t ["Il est possible de se connecter au Club avec plusieurs comptes. Un de ces comptes sera votre compte principal, avec un profil de professeur. Les autres comptes pourront avoir un profil d’élève."])]
          [:p (t ["Attention, vous ne pouvez pas gérer vos vrais élèves depuis différents comptes, même s’ils ont un profil de professeur."])]
        ]
      ]
    ]
  ])

(defn page-help
  []
  (if (not @(rf/subscribe [:authenticated]))
    [page-help-guest]
    (case @(rf/subscribe [:profile-quality])
      "scholar" [page-help-scholar]
      "teacher" [page-help-teacher]
      [page-help-guest])))

(defn school->menu-item
  [school]
  ^{:key (:id school)} [:> (bs 'MenuItem)
                           {:eventKey (:id school)}
                           (:name school)])

(defn teacher->menu-item
  [{:keys [id lastname]}]
  ^{:key id} [:> (bs 'MenuItem) {:eventKey id} lastname]
  )

(defn teachers-dropdown
  []
  (let [teachers-list @(rf/subscribe [:profile-teachers-list])]
    [:> (bs 'DropdownButton)
        {:title @(rf/subscribe [:profile-teacher-pretty])
         :on-select #(rf/dispatch [:profile-teacher %])}
       ^{:key "no-teacher"} [:> (bs 'MenuItem) {:eventKey "no-teacher"}
                                               (t ["Pas de professeur"])]
       (when (not (empty? teachers-list))
         [:> (bs 'MenuItem) {:divider true}])
       (when (not (empty? teachers-list))
         (map teacher->menu-item teachers-list))]))

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
                                  :event-id :profile-firstname}]
        school [:> (bs 'DropdownButton)
                   {:title @(rf/subscribe [:profile-school-pretty])
                    :on-select #(rf/dispatch [:profile-school %])}
                  [:> (bs 'MenuItem) {:eventKey "fake-id-no-school"}
                                     (t ["Aucun établissement"])]
                  [:> (bs 'MenuItem) {:divider true}]
                  (map school->menu-item (club.db/get-schools!))
               ]
       ]
    [:div
      [:div.jumbotron
        [:h2 (t ["Votre profil"])]]
      [:form {:role "form"}
        [:div {:style {:margin-bottom "1em"}}  ; TODO CSS
          [:> (bs 'ButtonToolbar)
            [:> (bs 'ToggleButtonGroup)
                {:type "radio"
                 :name "quality"
                 :value @(rf/subscribe [:profile-quality])
                 :defaultValue "scholar"
                 :on-change #(rf/dispatch [:profile-quality %])}
              [:> (bs 'ToggleButton) {:value "scholar"} (t ["Élève"])]
              [:> (bs 'ToggleButton) {:value "teacher"} (t ["Professeur"])]]]]
        [:div {:style {:margin-bottom "1em"}}  ; TODO CSS
          school]
        (if (= "scholar" @(rf/subscribe [:profile-quality]))
          [:div {:style {:margin-bottom "1em"}}  ; TODO CSS
            [:> (bs 'ControlLabel) (t ["Professeur : "])]
            [teachers-dropdown @(rf/subscribe [:profile-school])]]
          "")
        lastname
        (if (= "scholar" @(rf/subscribe [:profile-quality]))
          firstname
          "")
        [:> (bs 'Button)
          {:style {:margin "1em"}  ; TODO CSS
           :on-click #(rf/dispatch [:profile-cancel])
           :bsStyle "danger"}
          "Annuler les modifications"]
        [:> (bs 'Button)
          {:style {:margin "1em"}  ; TODO CSS
           :on-click #(rf/dispatch [:profile-save])
           :bsStyle "success"} "Enregistrer les modifications"]
      ]
    ]))

(def react-select (getValueByKeys js/window "deps" "react-select"))
(def Creatable (getValueByKeys react-select "Creatable"))

(defn groups-select
  [scholar-id]
  (let [value @(rf/subscribe [:groups-value scholar-id])]
    [:span {:style {:margin-left "1em"}}  ; TODO CSS
      [:> Creatable
         {:multi true
          :options (map groups-option @(rf/subscribe [:groups]))
          :on-change #(rf/dispatch [:groups-change [scholar-id %]])
          :placeholder (t ["Assigner à un groupe…"])
          :value value}]]))

(defn scholar-li-group-input
  [scholar]
  ^{:key (:id scholar)} [:li (:lastname scholar) " " (:firstname scholar)
                         (groups-select (:id scholar))])

(defn group-link
  [group]
  ^{:key group}
  [:span
    {:style {:margin "1em"}}  ; TODO CSS
    group])

(defn scholar-li
  [scholar]
  ^{:key (str (:lastname scholar) (:firstname scholar))}
  [:li (:lastname scholar) " " (:firstname scholar)])

(defn format-group
  [[group scholars]]
  ^{:key group} [:div
                  [:h3 group]
                  [:ul.nav
                    (map scholar-li (sort scholar-comparator scholars))]])

(defn groups-list-of-lists
  [groups-map groups]
  (let [; {"id1" {:k v} "id2" {:k v}} -> ({:k v} {:k v})
        lifted-groups-map (map second groups-map)
        scholars-in-groups
         (map (fn [group]
                [group (filter #(some #{group} (:groups %))
                                         lifted-groups-map)]) groups)]
    (map format-group scholars-in-groups)))

(defn page-groups
  []
  (let [groups-data @(rf/subscribe [:groups-page])
        groups @(rf/subscribe [:groups])
        lifted-groups (map #(merge {:id (first %)} (second %)) groups-data)]
    [:div
      [:div.jumbotron
        [:h2 (t ["Groupes"])]
        [:p (t ["Assignez chacun de vos élèves à un ou plusieurs groupes"])]]
        [:p (t ["Un groupe peut correspondre : à des classes entières, à des demis-groupes d’une classe, à des élèves ayant des besoins spécifiques (remédiation ou approfondissement) au sein de l’Accompagnement Personnalisé ou non…"])]
      [:> (bs 'Grid)
        [:> (bs 'Row)
          [:> (bs 'Col) {:xs 6 :md 6}
            [:h2 (t ["Vos élèves"])]
            [:ul.nav {:max-height "30em" :overflow-y "scroll"}  ; TODO CSS
              (doall (map scholar-li-group-input
                          (sort scholar-comparator lifted-groups)))]]
          [:> (bs 'Col) {:xs 6 :md 6}
            [:h2 (t ["Vos groupes"])]
            [:div (map group-link groups)]
            [:div {:max-height "30em" :overflow-y "scroll"}  ; TODO CSS
              (groups-list-of-lists groups-data groups)]]
         ]]
      [:> (bs 'Button)
        {:style {:margin "1em"}  ; TODO CSS
         :on-click #(rf/dispatch [:groups-cancel])
         :bsStyle "danger"}
        "Annuler les modifications"]
      [:> (bs 'Button)
        {:style {:margin "1em"}  ; TODO CSS
         :on-click #(rf/dispatch [:groups-save])
         :bsStyle "success"} "Enregistrer les modifications"]]))

(defn page-forbidden
  []
  [:div.jumbotron
    [:h2 (t ["Désolé, il faut se connecter pour accéder à cette page."])]])

(defn main-panel []
  (fn []
    (let [authenticated  @(rf/subscribe [:authenticated])
          quality        @(rf/subscribe [:profile-quality])
          current-page   @(rf/subscribe [:current-page])]
      [:div
        [:div.container
          [nav-bar]
          (if (or authenticated
                  (some #{current-page} [:landing :help]))
            ; TODO: try to replace the case below with this:
            ; (array (symbol (str "page-" (subs (str current-page) 1))))
            (case current-page
              :landing [page-landing]
              :help [page-help]
              :profile [page-profile]
              :groups [page-groups])
            [page-forbidden]
          )
          [footer]
        ]
        (when (and true config/debug?)
          [:pre {:style {:position "absolute" :bottom "0px" :width "100%"}}
            (with-out-str (pprint @app-db))])
      ]
    )))
