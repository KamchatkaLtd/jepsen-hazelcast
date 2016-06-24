(ns jepsen.hazelcast
  (:require [clojure.tools.logging :refer :all]
            [jepsen [db    :as db]
                    [control :as c]
                    [tests :as tests]]
            [jepsen.os.debian :as debian]))

(defn db
  "Hazelcast DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (info node "installing HC" version))

    (teardown! [_ test node]
      (info node "tearing down HC"))))

(defn hc-test
  [version]
  (assoc tests/noop-test
  	     :os  "debian-os"
  	     :db  (db version)
         :ssh { :username "alexey" }))
