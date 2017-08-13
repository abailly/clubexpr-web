(ns club.utils
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [goog.object :refer [getValueByKeys]]
            [reagent.core :as r]
            [webpack.bundle]))
 
(defn parse-url
  [url]
  (let [after-hash (get (str/split url "#/") 1)
        after-hash-splitted (str/split after-hash "?")
        before-qmark (get after-hash-splitted 0)
        page (keyword (if (empty? before-qmark) "landing" before-qmark))
        after-qmark (get after-hash-splitted 1)
        array (filter (complement #(some #{%} ["&" "=" ""]))
                (str/split after-qmark #"(&|=)"))
        query-params (keywordize-keys (apply hash-map array))
        ]
      {:page page
       :query-params query-params}))

(defn get-url-all!
  []
  (-> js/window .-location .-href))

(defn get-url-root!
  []
  (let [hostname (-> js/window .-location .-hostname)
        port (-> js/window .-location .-port)]
    (if (empty? port)
      hostname
      (str hostname ":" port "/"))))

;(def FormControl (r/adapt-react-class (.-FormControl js/ReactBootstrap)))
(def FormControl
     (r/adapt-react-class (getValueByKeys js/window "deps" "react-bootstrap"
                                                    "FormControl")))

; https://gist.github.com/metametadata/3b4e9d5d767dfdfe85ad7f3773696a60
(defn FormControlFixed
  "FormControl without cursor jumps in text/textarea elements.
  Problem explained:
  https://stackoverflow.com/questions/28922275/in-reactjs-why-does-setstate-behave-differently-when-called-synchronously/28922465#28922465
  I haven't tested it much in IE yet, so if it breaks in IE see this:
  https://github.com/tonsky/rum/issues/86
  Usage example:
  [FormControlFixed {:type       :text
                     :value      @ui-value
                     :max-length 10
                     :on-change  #(on-change-text (.. % -target -value))}]"
  [{:keys [value on-change] :as _props}]
  {:pre [(ifn? on-change)]}
  (let [local-value (atom value)]
    (r/create-class
      {:display-name            "FormControlFixed"
       :should-component-update
         ; Update only if value is different from the rendered one or...
         (fn [_ [_ old-props] [_ new-props]]
          (if (not= (:value new-props) @local-value)
            (do
              (reset! local-value (:value new-props))
              true)
            ; other props changed
            (not= (dissoc new-props :value)
                  (dissoc old-props :value))))

       :render
         (fn [this]
          [FormControl (-> (r/props this)
                           ; use value only from the local atom
                           (assoc :value @local-value)
                           (update :on-change
                                   (fn wrap-on-change [original-on-change]
                                     (fn wrapped-on-change [e]
                                       ; render immediately to sync DOM and
                                       ; virtual DOM
                                       (reset! local-value (.. e -target -value))
                                       (r/force-update this)

                                       ; this will presumably update the value
                                       ; in global state atom
                                       (original-on-change e)))))])})))
