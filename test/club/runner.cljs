(ns club.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [club.utils-test]))

(doo-tests 'club.utils-test)
