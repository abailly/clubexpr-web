(ns club.utils
 (:require [clojure.string :as str]
           [clojure.walk :refer [keywordize-keys]]))
 
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
      {:page page}))
