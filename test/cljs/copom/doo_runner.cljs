(ns copom.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [copom.core-test]))

(doo-tests 'copom.core-test)

