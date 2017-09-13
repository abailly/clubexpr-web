(ns club.expr
  (:require [goog.object :refer [getValueByKeys]]
            [clojure.walk :refer [keywordize-keys]]
            [club.utils :refer [jsx->clj
                                js->clj-vals
                                groups-option
                                scholar-comparator
                                FormControlFixed]]))

(def clubexpr (getValueByKeys js/window "deps" "clubexpr"))

(defn populate-properties
  [expr-obj]
  (let [properties (.-properties clubexpr)
        expr-obj-clj (-> expr-obj js->clj keywordize-keys)
        expr-properties (-> expr-obj
                            (getValueByKeys "expr")
                            properties
                            jsx->clj
                            js->clj-vals
                            )]
    (assoc expr-obj-clj :properties expr-properties)))

(def reified-expressions
  (map populate-properties (.-expressions clubexpr)))

