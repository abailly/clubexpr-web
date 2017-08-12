(ns club.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [club.utils :refer [parse-url]]))

(deftest parse-urls
  (testing "page only"
    (is (= (parse-url "/#/") {:page "landing"}))
    (is (= (parse-url "/#/foo") {:page "foo"}))
  )
)
