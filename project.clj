(defproject aspi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :min-lein-version "2.0.0"
  :url "http://aspi-asstrid.herokuapp.com"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :heroku {:app-name "aspi-asstrid"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [prismatic/schema "1.1.9"]
                 [metosin/compojure-api "2.0.0-alpha26"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [org.clojure/core.memoize "0.8.2"]
                 [ring-cors "0.1.13"]
                 [clj-http "3.10.0"]
                 [org.clojure/data.json "1.0.0"]
                 ;environment
                 [environ "1.1.0"]]
  :main ^:skip-aot aspi.core
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :target-path "target/%s"
  :profiles {:uberjar {:main aspi.core :aot :all}})
