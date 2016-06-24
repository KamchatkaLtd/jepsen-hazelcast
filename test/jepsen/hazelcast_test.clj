(ns jepsen.hazelcast-test
  (:require [clojure.test :refer :all]
            [jepsen.core :as jepsen]
            [jepsen.hazelcast :as hc]))

(deftest hc-test
  (is (:valid? (:results (jepsen/run! (hc/hc-test "3.6.3"))))))
