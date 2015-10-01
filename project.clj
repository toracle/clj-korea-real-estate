(defproject clj-korea-real-estate "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/data.json "0.2.6"]]
  :profiles {:dev {:plugins [[lein-midje "3.1.1"]]}})
