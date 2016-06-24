(ns jepsen.hazelcast
  (:require [jepsen.tests :as tests]
            [jepsen.control :as control]))

(defn hc-test
  [version]
  (assoc tests/noop-test
         :ssh { :username "alexey" }))
