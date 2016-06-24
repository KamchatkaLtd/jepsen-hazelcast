(ns jepsen.hazelcast
  (:require [jepsen.tests :as tests]))

(defn hc-test
  [version]
  tests/noop-test)
