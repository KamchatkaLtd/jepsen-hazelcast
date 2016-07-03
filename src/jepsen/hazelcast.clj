(ns jepsen.hazelcast
  (:require [clojure.tools.logging :refer :all]
            [jepsen [db    :as db]
                    [control :as c]
                    [tests :as tests]]
            [jepsen.os.debian :as debian]))


(defn install!
  "Install Hazelcast!"
  [node version]
  (c/su
    (let [jar (str "hazelcast-" version ".jar")
          uri (str "http://central.maven.org/maven2/"
                   "com/hazelcast/hazelcast/" version "/" jar)]

         (loop []
           (info node "downloading Hazelcast")
           (c/exec :wget :-c uri)
           (c/exec :wget :-c (str uri ".asc.sha1"))
           (when (try
                      (c/exec :sha1sum :-c (str jar ".asc.sha1"))
                      false
                      (catch RuntimeException e
                        (info "SHA failed" (.getMessage e))
                        true))
                 (recur)))

        (info node "installing Hazelcast")
        (debian/install ["openjdk-8-jre-headless"]
        (s/exec :ln :-sf jar :hazelcast.jar))
    )))

(defn configure!
  "Configures Hazelcast."
  [node test]
  (c/su
    (info node "configuring Hazelcast")
    (c/exec :echo
            (-> "hazelcast.xml"
                io/resource
                slurp
                (str/replace "$NAME"  (name node))
                (str/replace "$HOSTS" (json/generate-string
                                        (vals (c/on-many (:nodes test)
                                                         (net/local-ip))))))
            :> "hazelcast.xml")))

(defn start!
  [node]
  "Starts Hazelcast."
  (info node "starting Hazelcast")
  (c/su (c/exec :java :-cp :hazelcast.jar "-Dhazelcast.config=hazelcast.xml" :com.hazelcast.examples.StartServer))

  (wait 60 :green)
  (info node "Hazelcast ready"))

(defn stop!
  "Shuts down Hazelcast."
  [node]
  (c/su
    (meh (c/exec :killall :-9 :hazelcast)))
  (info node "Hazelcast stopped"))

(defn db
  "Hazelcast DB for a particular version."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (doto node
        (install! version)
        (configure! test)
        (start!)))

    (teardown! [_ test node]
      ;; Leave system up, to collect logs, analyze post mortem, etc
      )))

(defn hc-test
  [version]
  (assoc tests/noop-test
  	     :os  debian/os
  	     :db  (db version)))
