(defproject tilps "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :exclusions [org.eclipse.jetty/jetty-server]
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.eclipse.jetty/jetty-server "9.4.27.v20200227"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [compojure "1.6.2"]
                 [com.datomic/datomic-pro "1.0.6202" :exclusions [[com.google.guava/guava]]]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [clojure.java-time "0.3.2"]
                 [mount "0.1.16"]
                 [org.clojure/clojurescript "1.10.764"]]
  :plugins [[lein-ring "0.12.5"]
            [lein-cljsbuild "1.1.8"]]
  :cljsbuild {
    :builds [{
        ; The path to the top-level ClojureScript source directory:
        :source-paths ["src-cljs"]
        ; The standard ClojureScript compiler options:
        ; (See the ClojureScript compiler documentation for details.)
        :compiler {
          :output-to "resources/public/main.js"  ; default: target/cljsbuild-main.js
          :optimizations :whitespace
          :pretty-print true}}]}
  :ring {:init tilps.core/init
         :handler tilps.handler/app
         :nrepl {:start? true}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]]
         :source-paths ["dev/clj"]}})
