(ns club.views
  (:require [re-frame.core :as re-frame]))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div
        [:h1 "Club des Expressions"]
        [:p "Bonjour " @name]])))
