(ns club.utils
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [goog.object :refer [getValueByKeys]]
            [reagent.core :as r]
            [webpack.bundle]))
 
(defn error
  [where]
  #(js/alert (str where ": " %)))

(defn data-from-js-obj
  [obj]
  (-> obj js->clj
          keywordize-keys
          :data))

(defn scholar-comparator
  [scholar1 scholar2]
  (let [ln1 (:lastname scholar1)
        ln2 (:lastname scholar2)
        fn1 (:firstname scholar1)
        fn2 (:firstname scholar2)]
    (if (= ln1 ln2)
      (compare fn1 fn2)
      (compare ln1 ln2))))

(defn groups-option
  [option-str]
  {:value option-str :label option-str})

(defn parse-url
  [url]
  (let [splitted-url (str/split url "#/")]
    (if (str/includes? url "#/")
      ; #/ in the URL
      (let [after-hash-slash (get splitted-url 1)
            page (if (empty? after-hash-slash)
                     :landing
                     (keyword after-hash-slash))]
        {:page page
         :query-params {}})
      ; no #/ in the URL
      (let [after-hash (get (str/split url "#") 1)]
        (if after-hash
          ; no #/ but a # in the URL with data after it
          (let [array (filter (complement #(some #{%} ["&" "=" ""]))
                              (str/split after-hash #"(&|=)"))
                query-params (keywordize-keys (apply hash-map array))]
            {:page :landing
             :query-params query-params})
          ; neither #/ nor an interesting # in the URL
          {:page :landing
           :query-params {}})))))

(defn get-url-all!
  []
  (-> js/window .-location .-href))

(defn get-url-root!
  []
  (let [protocol (-> js/window .-location .-protocol)
        hostname (-> js/window .-location .-hostname)
        protocol+hostname (str protocol "//" hostname)
        port (-> js/window .-location .-port)]
    (if (empty? port)
      protocol+hostname
      (str protocol+hostname ":" port "/"))))

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
