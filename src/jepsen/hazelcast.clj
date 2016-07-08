(ns jepsen.hazelcast
  (:require [cheshire.core           :as json]
            [clojure.java.io         :as io]
            [clojure.string          :as str]
            [chazel                  :as hz]
            [jepsen [core            :as jepsen]
                    [db              :as db]
                    [util            :as util :refer [meh timeout]]
                    [control         :as c :refer [|]]
                    [client          :as client]
                    [checker         :as checker]
                    [model           :as model]
                    [generator       :as gen]
                    [nemesis         :as nemesis]
                    [store           :as store]
                    [report          :as report]
                    [tests           :as tests]]
            [jepsen.checker.timeline :as timeline]
            [jepsen.control.net      :as net]
            [clj-http.client         :as http]
            [clojure.tools.logging   :refer :all]
            [jepsen.os.debian        :as debian]))

(defn wait
  "Waits for hazelcast to be healthy on the current node. Color is red,
  yellow, or green; timeout is in seconds."
  [timeout-secs color]
  (timeout (* 1000 timeout-secs)
           (throw (RuntimeException.
                    (str "Timed out after "
                         timeout-secs
                         " s waiting for hazelcast cluster recovery")))
    (loop []
      (when
        (try
          (c/exec :curl :-XGET
                  (str "http://localhost:9200/_cluster/health?"
                       "wait_for_status=" (name color)
                       "&timeout=" timeout-secs "s"))
          false
          (catch RuntimeException e true))
        (recur)))))

(defn install!
  "Install Hazelcast!"
  [node version]
  (c/su
    (let [jar (str "hazelcast-" version ".jar")
          uri (str "http://central.maven.org/maven2/"
                   "com/hazelcast/hazelcast/" version "/" jar)]

        (info node "downloading Hazelcast")
        (c/exec :wget :-c uri)

        (info node "installing Hazelcast")
        (debian/install ["openjdk-8-jre-headless"])
        (c/exec :ln :-sf jar :hazelcast.jar))))

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
