(defproject jepsen.hazelcast "0.1.0-SNAPSHOT"
  :description "Jepsen: Hazelcast"
  :url "http://www.kamchatkaltd.co.uk/jepsen-hazelcast"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [jepsen "0.0.9" :exclude net.java.dev.jna/jna]
                 [cheshire "5.6.3"]
                 [clj-http "3.1.0"]
                 [chazel "0.1.10"]
                 [org.hface/hface-client "0.1.3"]
                 [net.java.dev.jna/jna "4.1.0"]])
