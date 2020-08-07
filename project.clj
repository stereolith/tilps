(defproject tilps "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :exclusions [org.eclipse.jetty/jetty-server]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.eclipse.jetty/jetty-server "9.4.27.v20200227"]
                 [compojure "1.6.2"]
                 [com.datomic/client-pro "0.9.63"]
                 [ring/ring-defaults "0.3.2"]
                 [clojure.java-time "0.3.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:init tilps.core/init
         :handler tilps.handler/app
         :nrepl {:start? true}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]]}})